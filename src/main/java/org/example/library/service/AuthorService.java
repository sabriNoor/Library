package org.example.library.service;

import org.example.library.dto.AuthorResponse;
import org.example.library.dto.CreateAuthorRequest;
import org.example.library.dto.UpdateAuthorRequest;

import java.util.List;

public interface AuthorService {

    AuthorResponse createAuthor(CreateAuthorRequest author);

    AuthorResponse getAuthorById(Long id);

    List<AuthorResponse> getAllAuthors();

    List<AuthorResponse> searchAuthorsByName(String name);

    List<AuthorResponse> findAuthorsByEmailDomain(String domain);

    AuthorResponse updateAuthor(Long id, UpdateAuthorRequest author);

    void deleteAuthor(Long id);
}
