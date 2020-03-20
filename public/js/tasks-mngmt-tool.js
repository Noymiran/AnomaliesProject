angular.module('TaskMgmtTool', [])
    .value('PageState', {listing: []})
    .factory('TasksService', ['$http', function ($http) {
        return {
            load: function (uuid) {
                return $http.get('/tasks/' + uuid);
            },
            create: function (task) {
                return $http.post('/tasks', task);
            },
            update: function (task) {
                return $http.put('/tasks/' + task.uuid, task);
            },
            search: function (query) {
                return $http.post('/tasks/search', query, {cache: false});
            },
            delete: function (task) {
                return $http.delete('/tasks/' + task.uuid);
            }
        };
    }])
    .controller('CreateOrSearchTaskController', ['$scope', '$filter', 'TasksService', 'PageState', '$anchorScroll', '$timeout', function ($scope, $filter, TasksService, PageState, $anchorScroll, $timeout) {

        $scope.task = {};
        $scope.selectedMetric = {};
        $scope.detectorName = {};
        $scope.intervalUnitScheduler = {};
        $scope.outputChannel = {};
        $scope.metricsAndData = {};
        $scope.detectorName = [
            {
                id: 1, name: "Egads"
            }
        ];
        $scope.intervalUnitScheduler = [
            {id: 1, name: "Millis", value: "millis"},
            {id: 2, name: "Seconds", value: "seconds"},
            {id: 3, name: "Hours", value: "hours"},
            {id: 4, name: "Days", value: "days"},
        ];
        $scope.outputChannel = [
            {id: 1, name: "Email"}];
        $scope.metricsAndData = [
            {
                "name": "Prometheus",
                "metrics": [
                    {
                        "name": "Front End error",
                        "value": "clns_client_error"
                    },
                    {
                        "name": "Avg overhead",
                        "value": "rec_r8_overhead_avg{environment=\"inneractive\",cluster=\"r8-exchange\"}"
                    },
                    {
                        "name": "Avg health service failed",
                        "value": "consul_health_service_failed{environment=\"inneractive\",service=\"r8-exchange\"}"
                    },
                    {
                        "name": "AWS ELB un healthy host count avg",
                        "value": "cloudwatch_aws_elb_un_healthy_host_count_average{environment=\"inneractive\",load_balancer_name=\"Events-LB\",availability_zone=\"\"}"
                    },
                    {
                        "name": "AWS ELB healthy host count avg",
                        "value": "cloudwatch_aws_elb_healthy_host_count_average{environment=\"inneractive\",load_balancer_name=\"Events-LB\",availability_zone=\"\"}"
                    },
                    {

                        "name": "Avg health service passing",
                        "value": "consul_health_service_passing{environment=\"inneractive\",service=\"r8-exchange\"}"
                    },

                    {
                        "name": "Avg latency",
                        "value": "rec_r8_latency_avg{environment=\"inneractive\"}"
                    },
                    {
                        "name": "QPS",
                        "value": "dradis_qps_applicative{environment=\"inneractive\"}"
                    },
                    {
                        "name": "Adnet latency 95 percentile",
                        "value": "kamon_histogram_adnet_latency_95_percentile{environment=\"inneractive\"}"
                    }
                ],
                "intervalUnitData": [
                    {
                        "name": "Seconds",
                        "value": "s"
                    },
                    {
                        "name": "Minutes",
                        "value": "m"
                    },
                    {
                        "name": "Hours",
                        "value": "h"
                    },
                    {
                        "name": "Days",
                        "value": "d"
                    },
                    {
                        "name": "Weeks",
                        "value": "w"
                    }
                ]

            }
        ];


        $scope.getMetrics = function (selectedDataResource) {
            var filteredMetrics = $scope.metricsAndData.find(e => e.name === selectedDataResource)
            return filteredMetrics ? filteredMetrics.metrics : []
        };

        $scope.getData = function (selectedDataResource) {
            var filteredMetrics = $scope.metricsAndData.find(e => e.name === selectedDataResource)
            return filteredMetrics ? filteredMetrics.intervalUnitData : []
        };

        $scope.onMetricSelect = function () {
            $timeout(function () {
                $scope.task.metric = $scope.getMetrics($scope.task.dataResource).find(metric => metric.name === $scope.selectedMetric.name)
            }, 1);
        };

        $scope.search = function () {
            PageState.busy = "searching";
            $scope.searching = "searching";
            PageState.task = undefined;
            PageState.listing = [];
            var query = {
                name: $scope.task.name,
                detectorName: $scope.task.detectorName,
                dataResource: $scope.task.dataResource,
                intervalSchedulerUnit: $scope.task.intervalSchedulerUnit,
                intervalSchedulerLength: $scope.task.intervalSchedulerLength,
                intervalPeriodBackUnit: $scope.task.intervalPeriodBackUnit,
                intervalPeriodBackLength: $scope.task.intervalPeriodBackLength,
                metric: $scope.selectedMetric.name
            }
            TasksService.search(query).success(function (data, status, headers) {
                if (status == 200) {
                    if (angular.isArray(data)) {
                        PageState.listing = data;
                        $timeout(function () {
                            $anchorScroll("searchDetails")
                        }, 1)
                    }

                }
                $scope.searching = false;
                PageState.busy = false;
                $scope.task = {};
            }).catch(() => {
                $scope.searching = false;
                PageState.busy = false;
            })
        };

        $scope.create = function () {
            if ($scope.taskForm.$valid) {
                PageState.busy = "creating";
                $scope.creating = "creating";
                PageState.successMessage = undefined;
                var newTask = $scope.task;
                TasksService.create(newTask).success(function (data, status, headers) {
                    if (status == 201) {
                        PageState.successMessage = "Congratulations! Task " + newTask.name + " has been registered.";
                        newTask.uuid = headers("Location").split('/').pop()
                        PageState.task = newTask;
                        PageState.listing.push(newTask);
                        $timeout(function () {
                            $anchorScroll("taskDetails")
                        }, 1)
                    }
                    $scope.creating = false;
                    PageState.busy = false;
                    $scope.task = {};
                }).catch(() => {
                    $scope.creating = false;
                    PageState.busy = false;
                })
            }
        };

        $scope.mousedown = function () {
            PageState.successMessage = undefined;
        };

    }])
    .controller('ListTasksController', ['$scope', '$filter', 'PageState', function ($scope, $filter, PageState) {

        $scope.$watch(function () {
            return PageState.listing;
        }, function () {
            $scope.tasks = PageState.listing;
        });

        $scope.show = function (task) {
            PageState.task = task
        };

    }])
    .controller('TaskController', ['$scope', '$filter', 'TasksService', 'PageState', '$anchorScroll', '$timeout', function ($scope, $filter, TasksService, PageState, $anchorScroll, $timeout) {
        $scope.task = {};
        $scope.selectedMetric = {};
        $scope.detectorName = {};
        $scope.intervalUnitScheduler = {};
        $scope.outputChannel = {};
        $scope.metricsAndData = {};
        $scope.detectorName = [
            {
                id: 1, name: "Egads"
            }
        ];
        $scope.intervalUnitScheduler = [
            {id: 1, name: "Millis", value: "millis"},
            {id: 2, name: "Seconds", value: "seconds"},
            {id: 3, name: "Hours", value: "hours"},
            {id: 4, name: "Days", value: "days"},
        ];
        $scope.outputChannel = [
            {id: 1, name: "Email"}];


        $scope.metricsAndData = [
            {
                "name": "Prometheus",
                "metrics": [
                    {
                        "name": "Front End error",
                        "value": "clns_client_error"
                    },
                    {
                        "name": "Avg overhead",
                        "value": "rec_r8_overhead_avg{environment=\"inneractive\",cluster=\"r8-exchange\"}"
                    },
                    {
                        "name": "Avg health service failed",
                        "value": "consul_health_service_failed{environment=\"inneractive\",service=\"r8-exchange\"}"
                    },
                    {
                        "name": "AWS ELB un healthy host count avg",
                        "value": "cloudwatch_aws_elb_un_healthy_host_count_average{environment=\"inneractive\",load_balancer_name=\"Events-LB\",availability_zone=\"\"}"
                    },
                    {
                        "name": "AWS ELB healthy host count avg",
                        "value": "cloudwatch_aws_elb_healthy_host_count_average{environment=\"inneractive\",load_balancer_name=\"Events-LB\",availability_zone=\"\"}"
                    },
                    {

                        "name": "Avg health service passing",
                        "value": "consul_health_service_passing{environment=\"inneractive\",service=\"r8-exchange\"}"
                    },

                    {
                        "name": "Avg latency",
                        "value": "rec_r8_latency_avg{environment=\"inneractive\"}"
                    },
                    {
                        "name": "QPS",
                        "value": "dradis_qps_applicative{environment=\"inneractive\"}"
                    },
                    {
                        "name": "Adnet latency 95 percentile",
                        "value": "kamon_histogram_adnet_latency_95_percentile{environment=\"inneractive\"}"
                    }
                ],
                "intervalUnitData": [
                    {
                        "name": "Seconds",
                        "value": "s"
                    },
                    {
                        "name": "Minutes",
                        "value": "m"
                    },
                    {
                        "name": "Hours",
                        "value": "h"
                    },
                    {
                        "name": "Days",
                        "value": "d"
                    },
                    {
                        "name": "Weeks",
                        "value": "w"
                    }
                ]

            }
        ];

        $scope.getMetrics = function (selectedDataResource) {
            var filteredMetrics = $scope.metricsAndData.find(e => e.name === selectedDataResource)
            return filteredMetrics ? filteredMetrics.metrics : []
        };

        $scope.getData = function (selectedDataResource) {
            var filteredMetrics = $scope.metricsAndData.find(e => e.name === selectedDataResource)
            return filteredMetrics ? filteredMetrics.intervalUnitData : []
        };

        $scope.onMetricSelect = function () {
            $timeout(function () {
                $scope.task.metric = $scope.getMetrics($scope.task.dataResource).find(metric => metric.name === $scope.selectedMetric.name)
            }, 1);
        };

        $scope.$watch(function () {
            return PageState.task;
        }, function () {
            console.log("watch")
            $scope.task = PageState.task;
        });

        $scope.edit = function (task) {
            PageState.busy = "loading";
            PageState.successMessage = undefined;
            TasksService.load(task.uuid).success(function (data, status, headers) {
                if (status == 200) {
                    PageState.task = data;
                    $timeout(function () {
                        $scope.selectedMetric = {
                            name: data.metric.name
                        };
                        console.log("$scope.selectedMetric", $scope.selectedMetric);
                        $anchorScroll("editDetails")
                    }, 1)
                }
                PageState.busy = false;
                $scope.editMode = true
            });
        };

        $scope.cancel = function () {
            $scope.editMode = false;
        };

        $scope.update = function (task) {
            PageState.busy = "updating";
            $scope.updating = "updating";
            PageState.successMessage = undefined;
            TasksService.update(task).success(function (data, status, headers) {
                if (status == 200) {
                    PageState.successMessage = "Congratulations! Task " + task.name + " has been updated.";
                    $timeout(function () {
                        $anchorScroll("message")
                    }, 1)
                }
                PageState.busy = false;
                $scope.updating = false;
                $scope.editMode = false;
                $scope.task = {};
            })
                .catch(() => {
                    $scope.updating = false;
                    PageState.busy = false;
                })
        };

        $scope.delete = function (task) {
            if (confirm("Are you sure?")) {
                PageState.busy = "deleting";
                PageState.successMessage = undefined;
                var taskToDelete = task;
                TasksService.delete(taskToDelete).success(function (data, status, headers) {
                    if (status == 200) {
                        PageState.task = undefined;
                        PageState.successMessage = "Congratulations! Task " + taskToDelete.name + " has been removed.";
                        PageState.listing = PageState.listing && PageState.listing.filter(function (item) {
                            return item.uuid !== taskToDelete.uuid
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

