package org.fairdatapipeline.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class testStorage_root {
  String[] bad_remote_repos =
      new String[] {
        "host.xz:path/to/repo.git",
        "host.xz:/path/to/repo.git/",
        "/path/to/repo.git/",
        "path/to/repo.git/",
        "~/path/to/repo.git",
        "host.xz:~user/path/to/repo.git/"
      };
  String[] possible_remote_repos =
      new String[] {
        "ssh://user@host.xz:82/path/to/repo.git/",
        "ssh://user@host.xz/path/to/repo.git/",
        "ssh://host.xz:82/path/to/repo.git/",
        "ssh://host.xz/path/to/repo.git/",
        "ssh://user@host.xz/path/to/repo.git/",
        "ssh://host.xz/path/to/repo.git/",
        "ssh://user@host.xz/~user/path/to/repo.git/",
        "ssh://host.xz/~user/path/to/repo.git/",
        "ssh://user@host.xz/~/path/to/repo.git",
        "ssh://host.xz/~/path/to/repo.git",
        "user@host.xz:/path/to/repo.git/",
        "user@host.xz:~user/path/to/repo.git/",
        "user@host.xz:path/to/repo.git",
        "rsync://host.xz/path/to/repo.git/",
        "git://host.xz/path/to/repo.git/",
        "git://host.xz/~user/path/to/repo.git/",
        "http://host.xz/path/to/repo.git/",
        "https://host.xz/path/to/repo.git/",
        "file:///path/to/repo.git/",
        "file://~/path/to/repo.git/"
      };

  @Test
  void test_good_repo_strings() {
    Arrays.stream(possible_remote_repos)
        .forEach(
            s -> {
              String[] r = Storage_root.gitrepo_to_root(s);
              assertThat(r).hasSize(2);
              assertThat(r[0]).doesNotContain("null");
              assertThat(r[1]).doesNotContain("null");
            });
  }

  @Test
  void test_bad_repo_strings() {
    Arrays.stream(bad_remote_repos)
        .forEach(
            s -> {
              String[] r = Storage_root.gitrepo_to_root(s);
              assertThat(r).isEmpty();
            });
  }
}
