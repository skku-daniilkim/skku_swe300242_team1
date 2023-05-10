# SKKU Social Hub

## Contributing
### Using GitFlow
Create new branch for each new feature, and then merge/PR into `dev` and eventually into `master`/`main` once the new version is done. (see my branch `backend-init` for more info)
1.  Create new branch from `dev` (replace `myfeature` with your branch name):
    ```shell
    git checkout -b myfeature dev
    ```
2.  Push the new branch and set the `origin` as an upstream:
    ```shell
    git push -u origin myfeature
    ```
    To ensure that everything is correct:
    ```shell
    git branch -vv
    ```
    Should output something like:
    ```shell
    *   myfeature    1ab2cde [origin/myfeature] ...
        main         f56g78g [origin/main] ...
    ```
    `[origin/myfeature]` shows that the upstream push was performed correctly. If it does not exist. Then something went wrong. Try again.

    `*` shows the current working branch. Must be right beside your created feature branch. If not, switch to your feature branch using:
    ```shell
    git checkout myfeature
    ```
3.  Work in that branch. Commit, following Angular's Commit Conventions: https://github.com/angular/angular/blob/22b96b9/CONTRIBUTING.md#-commit-message-guidelines.
4.  When ready to push changes, use:
    ```shell
    git push
    ```
5.  Once feature is done and all tests are passed, request Pull Request or ask to merge. It should be approved by someone from `CODEOWNERS` file.
6.  Developers that merge, should always merge with `--no-ff` flag.