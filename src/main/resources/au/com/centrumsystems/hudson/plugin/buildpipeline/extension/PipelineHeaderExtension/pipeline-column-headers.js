document.addEventListener("DOMContentLoaded", () => {
    const projectDataHolders = document.querySelectorAll(".ch-project-data-holder");

    projectDataHolders.forEach(dataHolder => {
        const { projectId, projectJson } = dataHolder.dataset;

        buildPipeline.projectProxies[projectId] = window["projectProxy" + projectId];

        const projectElement = document.getElementById("project-" + projectId);
        const projectData = JSON.parse(projectJson);
        projectElement.innerHTML = buildPipeline.projectCardTemplate(projectData);
    });
});
