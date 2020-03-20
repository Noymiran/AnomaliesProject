angular.module('AnomaliesMgmtTool', [])
    .value('PageState', {listing: []})
    .factory('AnomaliesTimeSeriesService', ['$http', function ($http) {
        return {
            load: function (uuid) {
                return $http.get('/anomalies/' + uuid);
            },
            getAll: function () {
                return $http.post('/anomalies/all');
            },
            getTask: function (uuid) {
                return $http.get('/tasks/' + uuid);
            },
            delete: function (anomaly) {
                return $http.delete('/anomalies/' + anomaly.uuid);
            }
        };
    }])
    .controller('GetAnomaliesController', ['$scope', '$filter', 'AnomaliesTimeSeriesService', 'PageState', '$anchorScroll', '$timeout', function ($scope, $filter, AnomaliesTimeSeriesService, PageState, $anchorScroll, $timeout) {

        $scope.getAll = function () {
            PageState.busy = "searching";
            PageState.anomaly = undefined;
            PageState.listing = [];
            AnomaliesTimeSeriesService.getAll().success(function (data, status, headers) {
                if (status == 200) {
                    if (angular.isArray(data)) {
                        PageState.listing = data;
                    }
                }
                PageState.busy = false;
            });
        };

        $scope.mousedown = function () {
            PageState.successMessage = undefined;
        };

    }])
    .controller('ListAnomaliesController', ['$scope', '$filter', 'PageState', function ($scope, $filter, PageState) {

        $scope.$watch(function () {
            return PageState.listing;
        }, function () {
            $scope.anomalies = PageState.listing;
        });

        $scope.show = function (anomaly) {
            PageState.anomaly = anomaly
        };
    }])
    .controller('AnomaliesController', ['$scope', '$filter', 'AnomaliesTimeSeriesService', 'PageState', '$anchorScroll', '$timeout', function ($scope, $filter, AnomaliesTimeSeriesService, PageState, $anchorScroll, $timeout) {
        $scope.$watch(function () {
            return PageState.anomaly;
        }, function () {
            $scope.anomaly = PageState.anomaly;
        });
        $scope.getTask = function (anomaly) {
            anomaly.task = {};
            AnomaliesTimeSeriesService.getTask(anomaly.taskID).success(function (data, status, headers) {
                if (status == 200) {
                    anomaly.task=data;
                }
            });
        };

        $scope.delete = function (anomaly) {
            if (confirm("Are you sure?")) {
                PageState.busy = "deleting";
                PageState.successMessage = undefined;
                var anomalyToDelete = anomaly;
                AnomaliesTimeSeriesService.delete(anomalyToDelete).success(function (data, status, headers) {
                    if (status == 200) {
                        PageState.anomaly = undefined;
                        PageState.successMessage = "Congratulations! Anomaly with ID " + anomalyToDelete.uuid + " has been removed.";
                        PageState.listing = PageState.listing && PageState.listing.filter(function (item) {
                            return item.uuid !== anomalyToDelete.uuid
                        });
                    }
                    PageState.busy = false;
                });
            }
        };


        $scope.mousedown = function () {
            PageState.successMessage = undefined;
        };

    }])
    .controller('MessageController', ['$scope', '$filter', 'PageState', function ($scope, $filter, PageState) {
        $scope.$watch(function () {
            return PageState.successMessage;
        }, function () {
            $scope.successMessage = PageState.successMessage;
        });

        $scope.mousedown = function () {
            PageState.successMessage = undefined;
        };
    }]);

