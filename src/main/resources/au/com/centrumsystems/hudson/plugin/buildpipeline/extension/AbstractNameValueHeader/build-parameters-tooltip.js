const buildId = document.querySelector('.pipeline-info .revision .title').dataset.buildId;
jQuery('#build-parameters-' + buildId).tooltip({
    bodyHandler: function() {
        return jQuery('#build-parameters-' + buildId).html();
    },
});