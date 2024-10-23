Behaviour.specify(".progress-console-onclick", 'progress-bar-click', 0, function (progressBarElement) {

    const dataContainer = document.querySelector(".fill-dialog-params-statusbar");
    const href = dataContainer.dataset.fillDialogHref;
    const title = dataContainer.dataset.fillDialogTitle;

    progressBarElement.addEventListener('click', () => {
        buildPipeline.fillDialog(href, title);
    });
});

Behaviour.specify(".console-icon-onlick", 'console-icon-click', 0, function (consoleIconElement) {

    const dataContainer = document.querySelector(".fill-dialog-params-console-output-icon");
    const href = dataContainer.dataset.fillDialogHref;
    const title = dataContainer.dataset.fillDialogTitle;

    consoleIconElement.addEventListener('click', () => {
        buildPipeline.fillDialog(href, title);
    });
});
