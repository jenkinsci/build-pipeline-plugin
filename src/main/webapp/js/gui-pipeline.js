	jQuery("pipelines").html();
	jQuery(document).ready(function(){
		
		bpObject.getAllFirstJobsName(window.location.href, function(t) {
			var firstJobSet = t.responseObject();
			var table = document.getElementById("build-pipeline-plugin-jobs-list");
			var rows = table.rows;
			var rowcount = rows.length, r, cells, cellcount, c, cell;
			for (r=0;r<rowcount; r++) {
				cells = rows[r].cells;
				cellcount = cells.length;
				for (c=0;c<cellcount;c++) {
					cell = cells[c];
					text = (cell.textContent || cell.innerText);
				
					for (var i = 0; i < firstJobSet.length ; i++) {
						if (text == firstJobSet[i]) {
							cell.style.color="red";
						}
					}
				}
			}
		});
		
		var instance = jsPlumb.getInstance({
			Container:"body"
		});
		
		var preJobName = null;
		var preJobBoxId = null;
		var preJobBoxEle = null;
		var startDragFlag = false;
		
		removeStartDragFlag = function(event) {
			startDragFlag = false;
			startDragTemplateFlag = false;
		}
		
		startDragArrow = function(event, spanBar) {
			var e = event ? event: window.event;
			e.preventDefault();
			preJobBoxId = "build-" + spanBar.parentNode.getAttribute("id").split("-")[1];
			mouseDownAndUpTimer = window.setTimeout(function(){
				preJobName = findRelationship(spanBar)[1];
				startDragFlag = true;
			}, 200);
		}
		
		var conn = null;
		mouseOverJobBox = function(target) {
			if (startDragFlag) {
				var currentJobBoxId = target.getAttribute('id');
				conn = jsPlumb.connect({
							source:preJobBoxId,
							target:currentJobBoxId,
							anchors : ["Right", "Left"],
						});
			} else if (startDragTemplateFlag) {
				var currentJobBoxId = target.getAttribute('id');
				conn = jsPlumb.connect({
							source:preJobBoxEle,
							target:currentJobBoxId,
							anchors : ["Right", "Right"],
						});
			}
		}
		
		mouseOutJobBox = function(target) {
			if (startDragFlag || startDragTemplateFlag) {
				jsPlumb.detach(conn);
			}
		}
		
		endDragArrow = function(target) {
			if (startDragFlag) {
				
				var jobBoxId = target.getAttribute('id');
				var nextJobName = __getJobNameFromId(document.getElementById(jobBoxId)).getAttribute('title');
				
				if (preJobName == nextJobName) {
					if(confirm("Do you wanna copy \""+nextJobName+"\"?")) {
						bpObject.triggerCopyJob(preJobName, window.location.href, function() {
							window.setTimeout(function(){
								window.location.reload(true);
							}, 10);
						});
						return;
					} else {
						return;
					}
				}
				
				if (preJobName != nextJobName) {
					if(!confirm("Do you wanna append \""+nextJobName+"\" to \""+preJobName+"\"?")) {
						jsPlumb.detach(conn);
						startDragFlag = false;
						return;
					}
				}
				
				bpObject.triggerAppendJobRelationship(preJobName, nextJobName, window.location.href, function(){
					
					window.setTimeout(function(){
					window.location.reload(true);
				}, 10);
				});
			} else {
				startDragFlag = false;
				jsPlumb.detach(conn);
				clearTimeout(mouseDownAndUpTimer);
			}
		}
		
		var jobTemplateName = null;
		var startDragTemplateFlag = false;
		startDragJobTemplate = function(event, jobNameLink) {
			var e = event ? event: window.event;
			e.preventDefault();
			preJobBoxEle = jobNameLink;
			mouseDownAndUpTimer = window.setTimeout(function(){
				startDragTemplateFlag = true;
					jobTemplateName = jobNameLink.text;
				}, 200);
			return ;
		}
		
		endDragUpdateFirstJob = function() {
			if (startDragTemplateFlag) {
				if(!confirm("Do you want to change the first job to \"" + jobTemplateName)) {
					jsPlumb.detach(conn);
					startDragTemplateFlag = false;
					return;
				}
				
				bpObject.triggerAddFirstJob(jobTemplateName, window.location.href, function() {
					window.setTimeout(function(){
						window.location.reload(true);
					}, 10);
				});
			} else {
				jsPlumb.detach(conn);
				startDragTemplateFlag = false;
				clearTimeout(mouseDownAndUpTimer);
			}
		}
		
		endDragJobTemplate = function(target) {
			if (startDragTemplateFlag) {
				var jobBoxId = target.getAttribute('id');
				var preJobName = __getJobNameFromId(document.getElementById(jobBoxId)).getAttribute('title');
				
				if (jobTemplateName == preJobName) {
					alert("Not allowed.");
					return;
				}
				
				if (jobTemplateName != preJobName) {
					if(!confirm("Do you want to append \""+jobTemplateName+"\" to \""+preJobName+"\"?")) {
						jsPlumb.detach(conn);
						startDragTemplateFlag = false;
						return;
					}
				}
				
				bpObject.triggerAppendJobRelationship(preJobName, jobTemplateName, window.location.href, function(){
					
					window.setTimeout(function(){
						window.location.reload(true);
					}, 10);
				});
			} else {
				jsPlumb.detach(conn);
				startDragTemplateFlag = false;
				clearTimeout(mouseDownAndUpTimer);
			}
		}
		
		
		findRelationship = function(thisJob) {
			// get the current job name
			var thisJobId = "build-" + thisJob.parentNode.getAttribute("id").split("-")[1];
			var thisJobInfoNode = document.getElementById(thisJobId);
			
			var currentJobName = __getJobNameFromId(thisJobInfoNode).getAttribute("title");
			var currentArrow = thisJobInfoNode.previousSibling.firstChild;
			var parentJobName = null;
			if (currentArrow == null) {
				parentJobName = __getJobNameFromId(thisJobInfoNode).getAttribute("title");
			} else {
				var coordinate = currentArrow.id.split("%");
				var coordinateY = null;
				var patt = null;
				var currentBuildPipeline = null;
				if (coordinate.length != 3) {
					coordinateY = coordinate[1];  // the coordinate of y
					currentBuildPipeline = currentArrow.parentNode.parentNode.previousSibling;
					var previousArrayId = null;
					
					do {
						coordinateY--;
						patt = new RegExp("("+coordinate[0]+"%"+coordinateY+"%\\d+)"+"|("+coordinate[0]+"%"+coordinateY+")");
						previousArrayId = patt.exec(currentBuildPipeline.outerHTML);
						currentBuildPipeline = currentBuildPipeline.previousSibling;
						var continueFlag = true;
						if (previousArrayId != null && previousArrayId[0].split("%").length == 3) {
							coordinate = previousArrayId[0].split("%");
							break;
						}
					} while(true);
				}
				var parentJobInfoNode = document.getElementById("build-" + coordinate[2]);
				parentJobName = __getJobNameFromId(parentJobInfoNode).getAttribute("title");
			}
			return [parentJobName, currentJobName]
		}
		
		 __getJobNameFromId = function(jobTableElement) {
			return jobTableElement.firstChild.firstChild.firstChild.firstChild.firstChild.firstChild;
		}
		 
		 removeRelationship = function(trash) {
			var result = findRelationship(trash);
			if(!confirm("Do you want to remove \""+result[1]+"\" from \""+result[0]+"\"?")) {
				startDragFlag = false;
				return;
			}
			if (result[1] == result[0]) {
				alert("This can not be removed.");
				startDragFlag = false;
				return;
			}
			startDragFlag = false;
			bpObject.triggerRemoveRelationship(result[0], result[1], window.location.href, function(){
				window.setTimeout(function(){
					window.location.reload(true);
				}, 10);
			});
		}
	});