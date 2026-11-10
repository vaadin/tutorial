#!/usr/bin/env bash
#
# Opens a pull request that updates main to the last step that GenerateSteps.java generated, if
# main doesn't match it. The update replaces every file except README.md and the .github
# directory, and removes the files that the last step doesn't have. The pull request comes from
# the update-main branch. If main already matches, the script closes an open pull request from
# that branch.
#
# Usage: update-main.sh <output-dir> [--push]
#
# Run it in a clone of vaadin/tutorial, with main checked out. Without --push, it only prints the
# changes. With --push, it also needs the GitHub CLI, and permission to push, to open pull
# requests, and to run workflows. Set DOCS_COMMIT to the vaadin/docs commit that the steps were
# generated from, to mention it in the commit message and the pull request.

set -euo pipefail

steps=$1
push=${2:-}
remote=origin
branch=update-main
title="Update main to the current tutorial"

last=$(tail -n 1 "$steps/steps.tsv" | cut -f1)
git_dir=$(git rev-parse --absolute-git-dir)
index=$(mktemp)
trap 'rm -f "$index"' EXIT

rm -f "$index"
GIT_INDEX_FILE=$index git read-tree HEAD
# Run git in the step's directory, so that the pathspecs refer to it.
(cd "$steps/$last" && GIT_INDEX_FILE=$index git -c core.autocrlf=false --git-dir="$git_dir" --work-tree=. \
    add --all -- . ':!README.md' ':!.github')
tree=$(GIT_INDEX_FILE=$index git write-tree)

if [[ $tree == $(git rev-parse "HEAD^{tree}") ]]; then
    echo "main matches the $last step."
    if [[ $push == --push ]]; then
        for pr in $(gh pr list --head "$branch" --base main --state open --json number --jq '.[].number'); do
            gh pr close "$pr" --delete-branch --comment "main matches the tutorial again, so this pull request is no longer needed."
        done
    fi
    exit 0
fi

echo "main doesn't match the $last step:"
git diff --stat HEAD "$tree"

existing=$(git ls-remote "$remote" "refs/heads/$branch" | cut -f1)
if [[ -n $existing ]]; then
    git fetch --quiet "$remote" "refs/heads/$branch"
    if [[ $(git rev-parse "$existing^{tree}") == "$tree" && $(git rev-parse "$existing^") == $(git rev-parse HEAD) ]]; then
        echo "The $branch branch already has these changes."
        exit 0
    fi
fi

message="The application at the end of the last step of the Vaadin tutorial."
if [[ -n ${DOCS_COMMIT:-} ]]; then
    message+=$'\n\n'"Generated from vaadin/docs@$DOCS_COMMIT."
fi
commit=$(git commit-tree "$tree" -p HEAD -m "$title" -m "$message")
echo "$branch: $commit"

if [[ $push != --push ]]; then
    echo "Not pushing. Run with --push to update the $branch branch and its pull request."
    exit 0
fi

git push "$remote" "+$commit:refs/heads/$branch"

body="The tutorial in [vaadin/docs](https://github.com/vaadin/docs/tree/${DOCS_COMMIT:-main}/articles/getting-started/tutorial) has changed, so \`main\` no longer matches the application at the end of its last step. This pull request updates \`main\` to that application, as CI generated it. It doesn't change \`README.md\` or \`.github\`. If a step was added to the tutorial, or removed from it, update the table of step branches in \`README.md\` before you merge.

\`\`\`
$(git diff --stat HEAD "$commit")
\`\`\`

GitHub doesn't run workflows for pull requests that a workflow opens, so CI runs on the \`$branch\` branch through a manual workflow run instead. Its checks appear on this pull request.

This pull request was opened by the CI workflow. It updates the pull request when the tutorial changes again, and closes it when \`main\` matches the tutorial."
pr=$(gh pr list --head "$branch" --base main --state open --json number --jq '.[0].number // empty')
if [[ -n $pr ]]; then
    gh pr edit "$pr" --title "$title" --body "$body"
else
    gh pr create --head "$branch" --base main --title "$title" --body "$body"
fi
gh workflow run ci.yml --ref "$branch"
