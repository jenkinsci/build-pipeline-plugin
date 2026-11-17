jQuery(function ($) {
    document.querySelectorAll('.pipeline-info .revision .title[data-build-id]').forEach(function (element) {
        var buildId = element.dataset.buildId;
        if (!$('#build-parameters-' + buildId).length) {
            return;
        }
        var trigger = $('#build-parameters-trigger-' + buildId);
        if (!trigger.length) {
            trigger = $(element);
        }
        trigger.tooltip({
            bodyHandler: function () {
                return $('#build-parameters-' + buildId).html();
            }
        });
    });
});
