#!/usr/bin/env bash
#
# Commits the steps that GenerateSteps.java generated as a chain of commits, one per step, and
# points the step/<page> branches at them. A step keeps its commit if its files and the commit of
# the previous step are unchanged, so only the steps that changed, and the steps after them, get
# new commits.
#
# Usage: commit-steps.sh <output-dir> [--push]
#
# Run it in a clone of vaadin/tutorial. Without --push, it only prints which branches it would
# update. Set DOCS_COMMIT to the vaadin/docs commit that the steps were generated from, to mention
# it in the commit messages.

set -euo pipefail

steps=$1
push=${2:-}
remote=origin

git fetch --quiet "$remote" "+refs/heads/step/*:refs/remotes/$remote/step/*"

index=$(mktemp)
trap 'rm -f "$index"' EXIT

parent=
refspecs=()
while IFS=$'\t' read -r page title; do
    branch=step/$page
    rm -f "$index"
    GIT_INDEX_FILE=$index git -c core.autocrlf=false --work-tree="$steps/$page" add --all
    tree=$(GIT_INDEX_FILE=$index git write-tree)

    commit=$(git rev-parse --quiet --verify "refs/remotes/$remote/$branch^{commit}" || true)
    if [[ -n $commit && $(git rev-parse "$commit^{tree}") == "$tree" \
            && $(git rev-parse --quiet --verify "$commit^" || true) == "$parent" ]]; then
        echo "$branch: unchanged"
    else
        message="The application of the Vaadin tutorial at the end of the $title step:"
        message+=$'\n'"https://vaadin.com/docs/latest/getting-started/tutorial/$page"
        if [[ -n ${DOCS_COMMIT:-} ]]; then
            message+=$'\n\n'"Generated from vaadin/docs@$DOCS_COMMIT."
        fi
        commit=$(git commit-tree "$tree" ${parent:+-p "$parent"} -m "$title" -m "$message")
        refspecs+=("+$commit:refs/heads/$branch")
        echo "$branch: $commit"
    fi
    parent=$commit
done < "$steps/steps.tsv"

if [[ ${#refspecs[@]} -eq 0 ]]; then
    echo "All step branches are up to date."
elif [[ $push == --push ]]; then
    git push "$remote" "${refspecs[@]}"
else
    echo "Not pushing. Run with --push to update the branches."
fi
