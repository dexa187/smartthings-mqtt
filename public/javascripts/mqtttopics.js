function MqttTableCtrl($scope, $http) {
	$http(jsRoutes.controllers.MqttBridge.listTopics()).success(function(data) {
			$scope.topics = data;
	});
	
	$scope.deleteTopic = function(){
		$http(jsRoutes.controllers.MqttBridge.delete(this.topic.topicName)).success(function(data) {
			$http(jsRoutes.controllers.MqttBridge.listTopics()).success(function(data) {
				$scope.topics = data;
			});
		});
	};
}