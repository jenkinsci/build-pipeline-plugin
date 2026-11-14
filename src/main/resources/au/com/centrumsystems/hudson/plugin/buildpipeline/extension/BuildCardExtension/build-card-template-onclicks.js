function parseDataAttributes(container) {
    return {
        id: parseInt(container.dataset.showSpinnerId, 10),
        buildExtId: container.dataset.rerunBuildExtId,
        dependencyIds: container.dataset.rerunBuildDependencyIds
            .split(",")
            .filter(Boolean)
            .map(id => parseInt(id, 10))
    };
}

function parseTriggerAttributes(container) {
    return {
        id: parseInt(container.dataset.showSpinnerId, 10),
        upstreamProjectName: container.dataset.upstreamProjectName,
        upstreamBuildNumber: parseInt(container.dataset.upstreamBuildNumber, 10),
        projectName: container.dataset.projectName,
        dependencyIds: container.dataset.dependencyIds
            .split(",")
            .filter(Boolean)
            .map(id => parseInt(id, 10))
    };
}

function handleDialogClick(dataContainer) {
    const href = dataContainer.dataset.fillDialogHref;
    const title = dataContainer.dataset.fillDialogTitle;
    buildPipeline.fillDialog(href, title);
}

function handleRerunClick(dataContainer) {
    const { id, buildExtId, dependencyIds } = parseDataAttributes(dataContainer);
    buildPipeline.showSpinner(id);
    buildPipeline.rerunBuild(id, buildExtId, dependencyIds);
}

function handleTriggerBuild(dataContainer) {
    const { id, upstreamProjectName, upstreamBuildNumber, projectName, dependencyIds } = parseTriggerAttributes(dataContainer);
    buildPipeline.showSpinner(id);
    buildPipeline.triggerBuild(id, upstreamProjectName, upstreamBuildNumber, projectName, dependencyIds);
}

function createClickHandler(parentElement, targetSelector, handlerFn) {
    const newHandler = function(event) {
        const clickTarget = event.target.closest(targetSelector);
        if (!clickTarget || !parentElement.contains(clickTarget)) return;

        handlerFn(clickTarget);
    };

    parentElement.addEventListener("click", newHandler);
}

const onclickElements = {
    ".bct-progress-bar-onclick": {
        handler: handleDialogClick
    },
    ".bct-console-icon-onclick": {
        handler: handleDialogClick
    },
    ".bct-rerun-successful-build-onclick": {
        handler: handleRerunClick
    },
    ".bct-rerun-failed-latest-build-onclick": {
        handler: handleRerunClick
    },
    ".bct-rerun-failed-manual-build-onclick": {
        handler: handleTriggerBuild
    },
    ".bct-rerun-failed-build-onclick": {
        handler: handleRerunClick
    },
    ".bct-trigger-manual-build-latest-onclick": {
        handler: handleTriggerBuild
    },
    ".bct-trigger-manual-build-onclick": {
        handler: handleTriggerBuild
    }
};

document.addEventListener("DOMContentLoaded", function () {
    const pipelineWrappers = document.querySelectorAll(".pipeline-wrapper");

    if (!pipelineWrappers.length) return;

    pipelineWrappers.forEach(wrapper => {
        Object.entries(onclickElements).forEach(function([selector, { handler }]) {
            createClickHandler(wrapper, selector, handler);
        });
    });
});
