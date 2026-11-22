"use strict";

// script do front integrado com a api do spring

const STATUS = {
  ASSISTINDO:     { rotulo: "Assistindo",     aba: "Assistindo",     cls: "status-assistindo" },
  QUERO_ASSISTIR: { rotulo: "Quero assistir", aba: "Quero assistir", cls: "status-quero-assistir" },
  ASSISTIDO:      { rotulo: "Assistido",      aba: "Assistidos",     cls: "status-assistido" },
};
const ORDEM_STATUS = ["ASSISTINDO", "QUERO_ASSISTIR", "ASSISTIDO"];
const TIPOS = { SERIE: "Série", FILME: "Filme", ANIME: "Anime" };

const state = {
  // dados e estado da tela
  user: null,
  midias: [],
  lista: [],
  stats: null,
  aba: "ASSISTINDO",
  busca: "",
  ordem: "recent",
  authModo: "entrar",
  dlg: null, // controle do modal aberto
};

const $app = document.getElementById("app");
const $dlg = document.getElementById("dlg");
const $toast = document.getElementById("toast");

/* funcoes uteis */

// limpa caracteres especiais pra nao dar erro no html
const esc = (valor) =>
  String(valor ?? "").replace(/[&<>"']/g, (caractere) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[caractere]));

const pad = (num) => String(num).padStart(2, "0");
const norm = (texto) => String(texto ?? "").normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();

function formatarMinutos(minutos) {
  const horas = Math.floor(minutos / 60);
  const restoMinutos = minutos % 60;
  if (horas === 0) return `${restoMinutos} min`;
  return restoMinutos === 0 ? `${horas} h` : `${horas} h ${restoMinutos} min`;
}

// formata a data pro padrao dia/mes/ano
function formatarData(iso) {
  if (!iso) return "";
  const [ano, mes, dia] = iso.split("-");
  return `${dia}/${mes}/${ano}`;
}

let toastTimer;
function toast(mensagem, erro = false) {
  $toast.textContent = mensagem;
  $toast.classList.toggle("error", erro);
  $toast.classList.add("show");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    $toast.classList.remove("show");
  }, erro ? 5000 : 2800);
}

function guardarUsuario(usuario) {
  try { usuario ? localStorage.setItem("streamstats.user", JSON.stringify(usuario)) : localStorage.removeItem("streamstats.user"); } catch (_) { /* ignora */ }
}
function lerUsuario() {
  try { return JSON.parse(localStorage.getItem("streamstats.user")); } catch (_) { return null; }
}

/* chamadas pra api */

// funcao padrao pra fazer os fetchs no backend
async function api(caminho, { method = "GET", body } = {}) {
  let res;
  try {
    res = await fetch(caminho, {
      method,
      headers: body !== undefined ? { "Content-Type": "application/json" } : {},
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch (_) {
    throw new Error("Não foi possível falar com o servidor. Confira se a aplicação está rodando.");
  }
  if (res.status === 204) return null;
  let dados = null;
  try { dados = await res.json(); } catch (_) { /* sem corpo */ }
  if (!res.ok) throw new Error((dados && dados.erro) || `Erro ${res.status}`);
  return dados;
}

async function carregarTudo() {
  const id = state.user.id;
  // busca o catalogo, a lista do usuario e as estatisticas tudo junto
  const [midias, lista, stats] = await Promise.all([
    api("/midias"),
    api(`/lista/usuario/${id}`),
    api(`/lista/usuario/${id}/estatisticas`),
  ]);
  state.midias = midias;
  state.lista = lista;
  state.stats = stats;
}

async function atualizarStats() {
  state.stats = await api(`/lista/usuario/${state.user.id}/estatisticas`);
}

/* cards de midia */

// monta o card da midia e se nao tiver imagem gera uma capa colorida
function poster(midia, extra = "") {
  const gradientes = [
    "linear-gradient(145deg, #1e3a8a, #0f172a)",
    "linear-gradient(145deg, #312e81, #1e1b4b)",
    "linear-gradient(145deg, #14532d, #052e16)",
    "linear-gradient(145deg, #701a75, #4a044e)",
    "linear-gradient(145deg, #7c2d12, #431407)",
    "linear-gradient(145deg, #831843, #500724)",
    "linear-gradient(145deg, #1f2937, #111827)",
  ];
  let codigo = 0;
  for (let i = 0; i < midia.titulo.length; i++) {
    codigo += midia.titulo.charCodeAt(i);
  }
  const corFundo = gradientes[codigo % gradientes.length];
  const img = midia.capaUrl
    ? `<img src="${esc(midia.capaUrl)}" alt="" loading="lazy" referrerpolicy="no-referrer">`
    : "";
  return `<div class="poster">
    <div class="art" style="background: ${corFundo}"><span class="art-title">${esc(midia.titulo)}</span></div>
    ${img}
    <span class="tag">${TIPOS[midia.tipo] || ""}</span>
    ${extra}
  </div>`;
}

// se o link da imagem der erro, remove ela e fica a capa padrao
document.addEventListener("error", (e) => {
  if (e.target.tagName === "IMG" && e.target.closest(".poster")) e.target.remove();
}, true);

/* telas */

// renderiza a tela conforme o usuario logado
function render() {
  if (!state.user) {
    $app.innerHTML = telaEntrada();
    return;
  }
  $app.innerHTML = telaPrincipal();
  renderConteudo();
}

function telaEntrada() {
  const criar = state.authModo === "criar";
  return `<section class="auth">
    <div class="auth-hero">
      <h1>StreamStats</h1>
      <p>Sua lista de séries, filmes e animes, com a conta de quanto tempo você já passou na frente da tela.</p>
      <div class="legend">
        ${ORDEM_STATUS.map((status) => `<span class="chip ${STATUS[status].cls}"><span class="dot"></span>${STATUS[status].rotulo}</span>`).join("")}
      </div>
    </div>
    <div class="auth-box">
      <div class="switch">
        <button type="button" class="btn small ${criar ? "ghost" : "primary"}" data-act="auth-modo" data-modo="entrar">Entrar</button>
        <button type="button" class="btn small ${criar ? "primary" : "ghost"}" data-act="auth-modo" data-modo="criar">Criar conta</button>
      </div>
      <form id="authForm" class="auth-form">
        ${criar ? `<div class="field"><label class="lbl" for="a-nome">Nome</label>
          <input class="in" id="a-nome" name="nome" required maxlength="100" autocomplete="name"></div>` : ""}
        <div class="field"><label class="lbl" for="a-email">E-mail</label>
          <input class="in" id="a-email" name="email" type="email" required autocomplete="email"></div>
        <div class="field"><label class="lbl" for="a-senha">Senha</label>
          <input class="in" id="a-senha" name="senha" type="password" required minlength="${criar ? 6 : 1}" maxlength="72"
            autocomplete="${criar ? "new-password" : "current-password"}">
          ${criar ? `<span class="hint">Pelo menos 6 caracteres.</span>` : ""}</div>
        <button class="btn tape wide" type="submit">${criar ? "Criar conta" : "Entrar"}</button>
      </form>
    </div>
  </section>`;
}

function contador() {
  const tempo = state.stats?.tempoTotal || { dias: 0, horas: 0, minutos: 0, totalMinutos: 0 };
  const stats = state.stats || { queroAssistir: 0, assistindo: 0, assistidos: 0, notaMedia: null };
  const total = stats.queroAssistir + stats.assistindo + stats.assistidos;
  const nota = stats.notaMedia == null ? "sem notas" : stats.notaMedia.toFixed(1).replace(".", ",");
  return `<section class="strip" aria-label="Tempo de tela">
    <div class="counter" role="img" aria-label="${tempo.dias} dias, ${tempo.horas} horas e ${tempo.minutos} minutos">
      <div class="cell"><span class="num">${pad(tempo.dias)}</span><span class="unit">dias</span></div>
      <span class="colon" aria-hidden="true">:</span>
      <div class="cell"><span class="num">${pad(tempo.horas)}</span><span class="unit">horas</span></div>
      <span class="colon" aria-hidden="true">:</span>
      <div class="cell"><span class="num">${pad(tempo.minutos)}</span><span class="unit">min</span></div>
    </div>
    <div class="facts">
      <p class="lead"> Contagem de Tempo Total</p>
      <p class="muted"> Número Total de episódios que você já assistiu até o momento.</p>
      <dl class="mini">
        <div><dt>Na lista</dt><dd>${total}</dd></div>
        <div><dt>Nota média</dt><dd>${nota}</dd></div>
        <div><dt>Horas vistas</dt><dd>${Math.floor(tempo.totalMinutos / 60)}</dd></div>
      </dl>
    </div>
  </section>`;
}

function contagem(status) {
  return state.lista.filter((item) => item.status === status).length;
}

function telaPrincipal() {
  const abas = ORDEM_STATUS.map((status) => `
    <button type="button" class="tab ${STATUS[status].cls} ${state.aba === status ? "on" : ""}" data-act="aba" data-aba="${status}" role="tab"
      aria-selected="${state.aba === status}">
      <span class="dot"></span>${STATUS[status].aba}<span class="count">${contagem(status)}</span>
    </button>`).join("");
  const catalogo = `
    <button type="button" class="tab status-catalogo ${state.aba === "CATALOGO" ? "on" : ""}" data-act="aba" data-aba="CATALOGO" role="tab"
      aria-selected="${state.aba === "CATALOGO"}">Catálogo<span class="count">${state.midias.length}</span></button>`;
  const opcoes = state.aba === "CATALOGO"
    ? [["recent", "Mais recentes"], ["titulo", "Título (A a Z)"]]
    : [["recent", "Atualizados há pouco"], ["titulo", "Título (A a Z)"], ["nota", "Maior nota"]];

  return `
    <header class="topbar">
      <div class="brand"><span class="brand-mark" aria-hidden="true"></span><span class="brand-name">StreamStats</span></div>
      <input class="search" id="busca" type="search" placeholder="Buscar por título" value="${esc(state.busca)}" aria-label="Buscar por título">
      <div class="who"><span>${esc(state.user.nome)}</span><button type="button" class="btn small ghost" data-act="sair">Sair</button></div>
    </header>
    ${contador()}
    <div class="bar">
      <div class="tabs" role="tablist">${abas}${catalogo}</div>
      <div class="tools">
        <label for="ordem" class="hint">Ordenar</label>
        <select id="ordem" data-ordem>${opcoes.map(([valor, rotulo]) => `<option value="${valor}" ${state.ordem === valor ? "selected" : ""}>${rotulo}</option>`).join("")}</select>
        ${state.aba === "CATALOGO" ? `<button type="button" class="btn small tape" data-act="nova-midia">Nova mídia</button>` : ""}
      </div>
    </div>
    <section id="conteudo"></section>`;
}

function ordenar(itens, getMidia, getNota, getRecente) {
  const lista = [...itens];
  if (state.ordem === "titulo") lista.sort((a, b) => getMidia(a).titulo.localeCompare(getMidia(b).titulo, "pt-BR"));
  else if (state.ordem === "nota" && getNota) lista.sort((a, b) => (getNota(b) ?? -1) - (getNota(a) ?? -1));
  else lista.sort((a, b) => getRecente(b) - getRecente(a));
  return lista;
}

function renderConteudo() {
  const $c = document.getElementById("conteudo");
  if (!$c) return;
  const termo = norm(state.busca.trim());
  const casa = (midia) => !termo || norm(midia.titulo).includes(termo);

  if (state.aba === "CATALOGO") {
    const itens = ordenar(state.midias.filter(casa), (midia) => midia, null, (midia) => midia.id);
    if (state.midias.length === 0) {
      $c.innerHTML = vazio("O catálogo está vazio", "Cadastre a primeira série, filme ou anime para começar a montar sua lista.", "nova-midia", "Cadastrar mídia");
    } else if (itens.length === 0) {
      $c.innerHTML = vazio("Nada encontrado", `Nenhum título do catálogo tem “${esc(state.busca)}” no nome.`);
    } else {
      $c.innerHTML = `<div class="grid">${itens.map(cardCatalogo).join("")}</div>`;
    }
    return;
  }

  const doStatus = state.lista.filter((item) => item.status === state.aba && casa(item.midia));
  const itens = ordenar(doStatus, (item) => item.midia, (item) => item.nota,
    (item) => Date.parse(item.dataAtualizacao) * 1000 + item.id % 1000);
  if (contagem(state.aba) === 0) {
    const msg = {
      ASSISTINDO: ["Nada em andamento", "Escolha um título no catálogo e marque como assistindo."],
      QUERO_ASSISTIR: ["Sua fila está vazia", "Guarde aqui o que você pretende ver depois."],
      ASSISTIDO: ["Nenhum título concluído", "Quando você terminar algo, ele aparece aqui."],
    }[state.aba];
    $c.innerHTML = vazio(msg[0], msg[1], "aba-catalogo", "Abrir o catálogo");
  } else if (itens.length === 0) {
    $c.innerHTML = vazio("Nada encontrado", `Nenhum título desta aba tem “${esc(state.busca)}” no nome.`);
  } else {
    $c.innerHTML = `<div class="grid">${itens.map(cardItem).join("")}</div>`;
  }
}

function vazio(titulo, texto, acao, rotulo) {
  return `<div class="empty"><h2>${titulo}</h2><p>${texto}</p>
    ${acao ? `<button type="button" class="btn tape" data-act="${acao}">${rotulo}</button>` : ""}</div>`;
}

function resumoItem(item) {
  const midia = item.midia;
  if (midia.tipo === "FILME") return item.status === "ASSISTIDO" ? "Filme visto" : "Filme";
  return `${item.episodiosAssistidos} de ${midia.totalEpisodios} episódios`;
}

function cardItem(item) {
  const midia = item.midia;
  const barra = item.status === "ASSISTINDO" && midia.totalEpisodios > 1
    ? `<div class="prog" aria-hidden="true"><span style="width:${Math.round((item.episodiosAssistidos / midia.totalEpisodios) * 100)}%"></span></div>`
    : "";
  const nota = item.nota ? `<span class="nota" title="Sua nota">${item.nota}</span>` : "";
  return `<article class="item ${STATUS[item.status].cls}">
    <button type="button" class="poster-btn" data-act="abrir-item" data-id="${item.id}" aria-label="Abrir ${esc(midia.titulo)}">
      ${poster(midia, barra + nota)}
    </button>
    <div class="cap"><h3>${esc(midia.titulo)}</h3><p class="meta">${resumoItem(item)}</p></div>
  </article>`;
}

function cardCatalogo(midia) {
  const naLista = state.lista.find((item) => item.midia.id === midia.id);
  const acao = naLista
    ? `<span class="chip ${STATUS[naLista.status].cls}"><span class="dot"></span>${STATUS[naLista.status].rotulo}</span>`
    : `<details class="add"><summary class="btn small tape">Adicionar à lista</summary>
        <div class="menu">${ORDEM_STATUS.map((status) => `
          <button type="button" class="menu-btn ${STATUS[status].cls}" data-act="adicionar" data-midia="${midia.id}" data-status="${status}">
            <span class="dot"></span>${STATUS[status].rotulo}</button>`).join("")}
        </div></details>`;
  const detalhe = midia.tipo === "FILME" ? `${midia.duracaoMediaEpisodioMin} min` : `${midia.totalEpisodios} episódios`;
  return `<article class="item">
    ${poster(midia)}
    <div class="cap"><h3>${esc(midia.titulo)}</h3><p class="meta">${[midia.ano, detalhe].filter(Boolean).join(", ")}</p></div>
    ${acao}
    <div class="acts">
      <button type="button" class="btn small ghost" data-act="editar-midia" data-id="${midia.id}">Editar</button>
      <button type="button" class="btn small danger" data-act="excluir-midia" data-id="${midia.id}">Excluir</button>
    </div>
  </article>`;
}

/* modais */

function abrirDialogo() {
  renderDialogo();
  if (!$dlg.open) $dlg.showModal();
}

function fecharDialogo() {
  state.dlg = null;
  if ($dlg.open) $dlg.close();
}

function renderDialogo() {
  if (!state.dlg) return;
  if (state.dlg.tipo === "item") {
    const item = state.lista.find((i) => i.id === state.dlg.id);
    if (!item) return fecharDialogo();
    $dlg.innerHTML = detalheItem(item);
  } else {
    const midia = state.dlg.id ? state.midias.find((x) => x.id === state.dlg.id) : null;
    $dlg.innerHTML = formMidia(midia);
  }
}

function detalheItem(item) {
  const midia = item.midia;
  const ehFilme = midia.tipo === "FILME";
  const semNota = item.status === "QUERO_ASSISTIR";
  const gasto = item.episodiosAssistidos * midia.duracaoMediaEpisodioMin;
  const sub = [TIPOS[midia.tipo], midia.genero, midia.ano].filter(Boolean).join(", ");
  return `<div class="dlg-body ${STATUS[item.status].cls}">
    <div class="dlg-poster">${poster(midia)}</div>
    <div class="dlg-main">
      <div class="dlg-head">
        <h2>${esc(midia.titulo)}</h2>
        <button type="button" class="btn small ghost" data-act="fechar" aria-label="Fechar">Fechar</button>
      </div>
      <p class="sub">${esc(sub)}</p>
      ${midia.sinopse ? `<p class="synopsis">${esc(midia.sinopse)}</p>` : ""}

      <div class="field">
        <span class="lbl" id="l-status">Status</span>
        <div class="seg-ctl" role="group" aria-labelledby="l-status">
          ${ORDEM_STATUS.map((status) => `
            <button type="button" class="seg-btn ${STATUS[status].cls} ${item.status === status ? "on" : ""}" data-act="status" data-status="${status}"
              aria-pressed="${item.status === status}"><span class="dot"></span>${STATUS[status].rotulo}</button>`).join("")}
        </div>
      </div>

      ${ehFilme ? "" : `<div class="field">
        <label class="lbl" for="prog">Episódios assistidos: <output id="progOut">${item.episodiosAssistidos} de ${midia.totalEpisodios}</output></label>
        <input type="range" id="prog" min="0" max="${midia.totalEpisodios}" value="${item.episodiosAssistidos}" data-total="${midia.totalEpisodios}">
        <span class="hint">Ao chegar no último episódio, o status vira Assistido sozinho.</span>
      </div>`}

      <div class="field">
        <span class="lbl" id="l-nota">Sua nota</span>
        <div class="notas" role="group" aria-labelledby="l-nota">
          ${Array.from({ length: 10 }, (_, k) => k + 1).map((n) => `
            <button type="button" class="nota-btn ${item.nota === n ? "on" : ""}" data-act="nota" data-nota="${n}"
              aria-pressed="${item.nota === n}" ${semNota ? "disabled" : ""}>${n}</button>`).join("")}
        </div>
        <span class="hint">${semNota ? "Você pode avaliar depois de começar a assistir." : "Clique na nota atual para remover."}</span>
      </div>

      <p class="spent">Tempo gasto neste título: ${formatarMinutos(gasto)}. Atualizado em ${formatarData(item.dataAtualizacao)}.</p>
      <div class="dlg-foot">
        <button type="button" class="btn danger" data-act="remover-item">Remover da lista</button>
      </div>
    </div>
  </div>`;
}

function formMidia(midia) {
  const valores = midia || { titulo: "", tipo: "SERIE", genero: "", ano: "", totalEpisodios: 12, duracaoMediaEpisodioMin: 24, capaUrl: "", sinopse: "" };
  const filme = valores.tipo === "FILME";
  return `<form id="midiaForm" class="dlg-form" data-id="${midia ? midia.id : ""}">
    <h2>${midia ? "Editar mídia" : "Nova mídia"}</h2>
    <div class="field"><label class="lbl" for="f-titulo">Título</label>
      <input class="in" id="f-titulo" name="titulo" required maxlength="200" value="${esc(valores.titulo)}"></div>
    <div class="row2">
      <div class="field"><label class="lbl" for="f-tipo">Tipo</label>
        <select class="in" id="f-tipo" name="tipo" data-tipo>
          ${Object.entries(TIPOS).map(([k, r]) => `<option value="${k}" ${valores.tipo === k ? "selected" : ""}>${r}</option>`).join("")}
        </select></div>
      <div class="field"><label class="lbl" for="f-genero">Gênero</label>
        <input class="in" id="f-genero" name="genero" maxlength="80" value="${esc(valores.genero)}"></div>
    </div>
    <div class="row2">
      <div class="field"><label class="lbl" for="f-eps">Episódios</label>
        <input class="in" id="f-eps" name="totalEpisodios" type="number" min="1" required value="${filme ? 1 : esc(valores.totalEpisodios)}" ${filme ? "disabled" : ""}>
        <span class="hint" id="f-eps-hint">${filme ? "Filmes contam como 1 episódio." : "Quantidade total da obra."}</span></div>
      <div class="field"><label class="lbl" for="f-dur">Duração média (min)</label>
        <input class="in" id="f-dur" name="duracaoMediaEpisodioMin" type="number" min="1" required value="${esc(valores.duracaoMediaEpisodioMin)}">
        <span class="hint">Em filmes, a duração total.</span></div>
    </div>
    <div class="row2">
      <div class="field"><label class="lbl" for="f-ano">Ano</label>
        <input class="in" id="f-ano" name="ano" type="number" min="1888" max="2100" value="${esc(valores.ano)}"></div>
      <div class="field"><label class="lbl" for="f-capa">Link da capa</label>
        <input class="in" id="f-capa" name="capaUrl" type="url" maxlength="500" placeholder="https://..." value="${esc(valores.capaUrl)}">
        <span class="hint">Opcional. Sem capa, o sistema gera um pôster.</span></div>
    </div>
    <div class="field"><label class="lbl" for="f-sinopse">Sinopse</label>
      <textarea class="in" id="f-sinopse" name="sinopse" rows="3" maxlength="1000">${esc(valores.sinopse)}</textarea></div>
    <div class="dlg-foot">
      <button type="button" class="btn ghost" data-act="fechar">Cancelar</button>
      <button type="submit" class="btn tape">${midia ? "Salvar alterações" : "Cadastrar mídia"}</button>
    </div>
  </form>`;
}

/* acoes da tela */

// atualiza so o item que mudou na lista
function trocarItem(atualizado) {
  const idx = state.lista.findIndex((item) => item.id === atualizado.id);
  if (idx >= 0) state.lista[idx] = atualizado;
}

async function executar(funcao, sucesso) {
  try {
    await funcao();
    if (sucesso) toast(sucesso);
  } catch (e) {
    toast(e.message, true);
  }
}

// recarrega as estatisticas e atualiza a tela
async function aposMudanca() {
  await atualizarStats();
  render();
  renderDialogo();
}

const acoes = {
  "auth-modo": (elemento) => { state.authModo = elemento.dataset.modo; render(); },

  sair: () => {
    guardarUsuario(null);
    Object.assign(state, { user: null, midias: [], lista: [], stats: null, aba: "ASSISTINDO", busca: "", ordem: "recent" });
    render();
  },

  aba: (elemento) => { state.aba = elemento.dataset.aba; state.ordem = "recent"; render(); },
  "aba-catalogo": () => { state.aba = "CATALOGO"; state.ordem = "recent"; render(); },

  "abrir-item": (elemento) => { state.dlg = { tipo: "item", id: Number(elemento.dataset.id) }; abrirDialogo(); },
  fechar: () => fecharDialogo(),

  adicionar: (elemento) => executar(async () => {
    const criado = await api("/lista", {
      method: "POST",
      body: { usuarioId: state.user.id, midiaId: Number(elemento.dataset.midia), status: elemento.dataset.status },
    });
    state.lista.push(criado);
    await aposMudanca();
  }, "Adicionado à lista"),

  status: (elemento) => executar(async () => {
    const atual = state.lista.find((item) => item.id === state.dlg.id);
    if (atual.status === elemento.dataset.status) return;
    trocarItem(await api(`/lista/${state.dlg.id}/status`, { method: "PATCH", body: { status: elemento.dataset.status } }));
    await aposMudanca();
  }),

  nota: (elemento) => executar(async () => {
    const atual = state.lista.find((item) => item.id === state.dlg.id);
    const nova = Number(elemento.dataset.nota);
    trocarItem(await api(`/lista/${state.dlg.id}/nota`, { method: "PATCH", body: { nota: atual.nota === nova ? null : nova } }));
    await aposMudanca();
  }),

  "remover-item": () => executar(async () => {
    const id = state.dlg.id;
    await api(`/lista/${id}`, { method: "DELETE" });
    state.lista = state.lista.filter((item) => item.id !== id);
    fecharDialogo();
    await aposMudanca();
  }, "Removido da lista"),

  "nova-midia": () => { state.dlg = { tipo: "midia", id: null }; abrirDialogo(); },
  "editar-midia": (elemento) => { state.dlg = { tipo: "midia", id: Number(elemento.dataset.id) }; abrirDialogo(); },

  "excluir-midia": (elemento) => {
    const id = Number(elemento.dataset.id);
    const midia = state.midias.find((x) => x.id === id);
    if (!confirm(`Excluir “${midia.titulo}” do catálogo? Ela também sai da lista de quem a adicionou.`)) return;
    return executar(async () => {
      await api(`/midias/${id}`, { method: "DELETE" });
      state.midias = state.midias.filter((x) => x.id !== id);
      state.lista = state.lista.filter((item) => item.midia.id !== id);
      await aposMudanca();
    }, "Mídia excluída");
  },
};

// escuta os cliques nos botoes da tela
document.addEventListener("click", (e) => {
  if (e.target === $dlg) return fecharDialogo(); // se clicar fora fecha o modal
  const elemento = e.target.closest("[data-act]");
  if (elemento && acoes[elemento.dataset.act]) acoes[elemento.dataset.act](elemento);
});

$dlg.addEventListener("close", () => { state.dlg = null; });

document.addEventListener("input", (e) => {
  if (e.target.id === "busca") {
    state.busca = e.target.value;
    renderConteudo();
  } else if (e.target.id === "prog") {
    document.getElementById("progOut").textContent = `${e.target.value} de ${e.target.dataset.total}`;
  }
});

document.addEventListener("change", (e) => {
  const elemento = e.target;
  if (elemento.matches("[data-ordem]")) {
    state.ordem = elemento.value;
    renderConteudo();
  } else if (elemento.id === "prog") {
    executar(async () => {
      trocarItem(await api(`/lista/${state.dlg.id}/progresso`, { method: "PATCH", body: { episodios: Number(elemento.value) } }));
      await aposMudanca();
    });
  } else if (elemento.matches("[data-tipo]")) {
    const eps = document.getElementById("f-eps");
    const filme = elemento.value === "FILME";
    eps.disabled = filme;
    if (filme) eps.value = 1;
    document.getElementById("f-eps-hint").textContent = filme ? "Filmes contam como 1 episódio." : "Quantidade total da obra.";
  }
});

document.addEventListener("submit", async (e) => {
  e.preventDefault();
  const form = e.target;
  const botao = form.querySelector("[type=submit]");
  const dados = Object.fromEntries(new FormData(form));
  botao.disabled = true;

  try {
    if (form.id === "authForm") {
      const criar = state.authModo === "criar";
      const usuario = criar
        ? await api("/auth/register", { method: "POST", body: { nome: dados.nome, email: dados.email, senha: dados.senha } })
        : await api("/auth/login", { method: "POST", body: { email: dados.email, senha: dados.senha } });
      state.user = { id: usuario.id, nome: usuario.nome, email: usuario.email };
      guardarUsuario(state.user);
      await carregarTudo();
      render();
    } else if (form.id === "midiaForm") {
      const tipo = dados.tipo;
      const corpo = {
        titulo: dados.titulo.trim(),
        tipo,
        genero: dados.genero.trim() || null,
        ano: dados.ano ? Number(dados.ano) : null,
        totalEpisodios: tipo === "FILME" ? 1 : Number(dados.totalEpisodios),
        duracaoMediaEpisodioMin: Number(dados.duracaoMediaEpisodioMin),
        capaUrl: dados.capaUrl.trim() || null,
        sinopse: dados.sinopse.trim() || null,
      };
      const id = form.dataset.id;
      const salvo = id
        ? await api(`/midias/${id}`, { method: "PUT", body: corpo })
        : await api("/midias", { method: "POST", body: corpo });
      if (id) {
        state.midias = state.midias.map((x) => (x.id === salvo.id ? salvo : x));
        // recarrega a lista pra atualizar os dados da midia alterada
        state.lista = await api(`/lista/usuario/${state.user.id}`);
      } else {
        state.midias.push(salvo);
      }
      fecharDialogo();
      await aposMudanca();
      toast(id ? "Alterações salvas" : "Mídia cadastrada");
    }
  } catch (err) {
    toast(err.message, true);
    botao.disabled = false;
  }
});

/* inicializacao */

// checa se ja tinha usuario salvo no navegador pra abrir direto
(async function iniciar() {
  state.user = lerUsuario();
  if (state.user) {
    try {
      await carregarTudo();
    } catch (e) {
      toast(e.message, true);
      if (/não encontrado/i.test(e.message)) { guardarUsuario(null); state.user = null; }
    }
  }
  render();
})();
