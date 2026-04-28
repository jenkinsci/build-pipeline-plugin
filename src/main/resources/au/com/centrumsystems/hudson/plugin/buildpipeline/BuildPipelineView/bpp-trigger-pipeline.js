Behaviour.specify(".bpp-trigger-pipeline-parameterized", 'BuildPipelineView.TriggerPipelineParameterized', 0, function(element) {
    element.addEventListener('click', function() {
        const href = element.dataset.fillDialogHref;
        const title = element.dataset.fillDialogTitle;
        buildPipeline.fillDialog(href, title);
    });
});

Behaviour.specify("#trigger-pipeline-button", 'BuildPipelineView.TriggerPipeline', 0, function(element) {
    element.addEventListener('click', function() {
        document.getElementById('triggerPipelineForm').submit();
    });
});
