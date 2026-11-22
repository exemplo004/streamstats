package com.example.streamstats.services;

import com.example.streamstats.dto.AtualizarUsuarioRequest;
import com.example.streamstats.dto.LoginRequest;
import com.example.streamstats.dto.RegistroRequest;
import com.example.streamstats.entities.Usuario;
import com.example.streamstats.exceptions.CredenciaisInvalidasException;
import com.example.streamstats.exceptions.RecursoNaoEncontradoException;
import com.example.streamstats.exceptions.RegraNegocioException;
import com.example.streamstats.repositories.ItemListaRepository;
import com.example.streamstats.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Cadastro, login, edição e remover
@Service
public class UsuarioService {

    // Responsável pelo caso de uso de usuário: cadastro, login, edição e remoção.
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ItemListaRepository itemListaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }

    public Usuario cadastrar(RegistroRequest dto) {

        // A senha original nunca é mantida, guardando apenas seu hash 
        String email = dto.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new RegraNegocioException("E-mail já cadastrado");
        }
        Usuario usuario = new Usuario(dto.nome().trim(), email, passwordEncoder.encode(dto.senha()));
        return usuarioRepository.save(usuario);
    }

    public Usuario autenticar(LoginRequest dto) {

        // Comparação para a senha digitada com o hash armazenado
        Usuario usuario = usuarioRepository.findByEmail(dto.email().trim().toLowerCase())
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!passwordEncoder.matches(dto.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        return usuario;
    }

    public Usuario atualizar(Long id, AtualizarUsuarioRequest dto) {
        Usuario usuario = findById(id);
        usuario.setNome(dto.nome().trim());
        return usuarioRepository.save(usuario);
    }

    // @Transactional novamente para manter a remoção do usuário e do seus itens
    @Transactional
    public void deletar(Long id) {
        findById(id);
        itemListaRepository.deleteByUsuarioId(id);
        usuarioRepository.deleteById(id);
    }
}
