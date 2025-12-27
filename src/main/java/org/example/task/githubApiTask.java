package org.example.task;

import java.util.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;



@SpringBootApplication
public class githubApiTask {

    public static void main(String[] args) {
        SpringApplication.run(githubApiTask.class, args);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

}

// records, exception

record Repo(
        String name,
        String ownerLogin,
        List<Branch> branches
) {}

record Branch(
        String name,
        String lastCommitSha
) {}

record githubRepo(
        String name,
        boolean fork,
        Owner owner
) {}

record Owner(String login) {}

record githubBranch(
        String name,
        Commit commit
) {}

record Commit(String sha) {}

record errorResponse(
        int status,
        String message
) {}

class userNotFoundException extends RuntimeException {
    userNotFoundException(String username) {
        super("User '" + username + "' doesn't exist");
    }
}


// REST

@RestController
@RequestMapping("/api/")
class repoController {
    private final repoService service;

    repoController(repoService service) {
        this.service = service;
    }

    @GetMapping("/{username}/repos")
    ResponseEntity<List<Repo>> getRepositories(@PathVariable String username) {
        return ResponseEntity.ok(service.getUserRepositories(username));
    }

}

@Service
class repoService {
    private final githubClient client;

    repoService(githubClient client) {
        this.client = client;
    }

    List<Repo> getUserRepositories(String username) {
        var repos = client.fetchRepositories(username);
        return Arrays.stream(repos)
                .filter(repo -> !repo.fork())
                .map(repo -> new Repo(
                        repo.name(),
                        repo.owner().login(),
                        client.fetchBranches(username, repo.name())
                ))
                .toList();
    }

}

@Component
class githubClient {
    private static final String GITHUB_API = "https://api.github.com";
    private final RestTemplate restTemplate;

    githubClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    githubRepo[] fetchRepositories(String username) {
        try {
            return restTemplate.getForObject(
                    GITHUB_API + "/users/{username}/repos",
                    githubRepo[].class,
                    username
            );
        } catch (HttpClientErrorException.NotFound e) {
            throw new userNotFoundException(username);
        }
    }

    List<Branch> fetchBranches(String username, String repoName) {
        var branches = restTemplate.getForObject(
                GITHUB_API + "/repos/{username}/{repo}/branches",
                githubBranch[].class,
                username,
                repoName
        );
        return Arrays.stream(branches != null ? branches : new githubBranch[0])
                .map(b -> new Branch(b.name(), b.commit().sha()))
                .toList();
    }

}


@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(userNotFoundException.class)
    ResponseEntity<errorResponse> handleUserNotFound(userNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new errorResponse(404, e.getMessage()));
    }
}