Handlebars.registerHelper('startScript', function() {
    return new Handlebars.SafeString("<script>");
});

Handlebars.registerHelper('endScript', function() {
    return new Handlebars.SafeString("</script>");
});
