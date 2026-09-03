jQuery(document).ready(function() {
    jQuery(".header").click(function() {
        var parent = jQuery(this).parent();
        var ba = parent.find(".build-actions");
        var bb = parent.find(".build-body");

        ba.add(bb).toggle('slow');
    });
});
