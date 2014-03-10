function MqttTableCtrl($scope, $http, $rootScope) {
	
	$scope.topics = new Array();
	
	$scope.switches={
              true: "col-md-2 panel panel-primary",
              false: "col-md-2 panel panel-default"
    }
	
	
	$scope.deleteTopic = function(){
		$http(jsRoutes.controllers.MqttBridge.delete(this.topic.topicName)).success(function(data) {
		});
			var index = $scope.topics.indexOf(this.topic);
			if (index > -1) {
    			$scope.topics.splice(index, 1);
    			//$scope.$apply();
			}
	};
	
	$scope.ws = new WebSocket(jsRoutes.controllers.MqttBridge.getTopic(pageTopic).webSocketURL());
    
	$scope.ws.onopen = function(){  
        console.log("Socket has been opened!");  
    };
    
    $scope.ws.onmessage = function(message) {
        listener(JSON.parse(message.data));
    };
    
    function listener(data) {
        var messageObj = data;
        var metadata = messageObj.topicName.split("/");
        messageObj.name = metadata[1];
        messageObj.type = metadata[0];
        switch (messageObj.type) {
        	case "switch":
        		messageObj.state = messageObj.value.switch?"On":"Off";
        		break;
        	case "motion":
        		messageObj.state = messageObj.value.motion?"Detected":"Not-Detected";
        		break;
        	case "contact":
        		messageObj.state = messageObj.value.motion?"Open":"Closed";
        		break;
        }
        
        console.log("Received data from websocket: ", messageObj);
        var index = $scope.topics.indexOf(messageObj);
		if (messageObj.value != ""){
			var found = false;
			for (var topic in $scope.topics){
				if($scope.topics[topic].topicName == messageObj.topicName){
    				$scope.topics[topic] = messageObj;
    				found = true;
    				$scope.talk(messageObj);
    			}
    		}
    		if (!found){
    			$scope.topics.push(messageObj);
    		}
		}else{
			for (var topic in $scope.topics){
				if($scope.topics[topic].topicName == messageObj.topicName){
    				$scope.topics.splice(topic, 1);
    			}
    		}
		}
		$scope.$apply();       
    }
    
    $scope.talk = function(topic){
    	var msgTxt = topic.name+" "+topic.state;
    	var msg = new SpeechSynthesisUtterance(msgTxt);
		window.speechSynthesis.speak(msg);
	};
	
	$scope.toggleSwitch = function(topic){
		var controlTopic = "switch/"+topic.name+"/control";
		var newState = !topic.value.switch;
		var controlValue = {"timestamp":moment().format(),"switch":newState,"hub":topic.value.hub,"system":topic.value.system}
		jsRoutes.controllers.MqttBridge.publish(controlTopic).ajax({
			data : JSON.stringify(controlValue),
			contentType : 'text/plain',
			success : function () {
				topic.value.switch = !topic.value.switch;
				$scope.$apply();
			}
		});
	}
}