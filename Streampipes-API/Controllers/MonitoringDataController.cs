using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Streampipes_API.Models;
using Streampipes_API.Interfaces;
using Streampipes_API.Services;
using InfluxDB.Client.Writes;
using InfluxDB.Client.Api.Domain;

namespace Streampipes_API.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class MonitoringDataController : ControllerBase
    {
        private readonly ILogger<MonitoringDataController> _logger;
        private readonly MongoDBService _mongoDBService;
        private readonly InfluxDBService _influxService;

        public MonitoringDataController(ILogger<MonitoringDataController> logger, MongoDBService monitoringService, InfluxDBService i)
        {
            _logger = logger;
            _mongoDBService = monitoringService;
            _influxService = i;
        }

        [HttpGet]
        public Task<IEnumerable<MonitoringDataResponse>> Get()
        {
            //_mongoDBService.Get();
            var results = _influxService.QueryAsync(async query =>
            {
                var flux = "from(bucket:\"test-bucket\") |> range(start: 0)";
                var tables = await query.QueryAsync(flux, "organization");
                return tables.SelectMany(table =>
                    table.Records.Select(record =>
                        new MonitoringDataResponse
                        {
                            Timestamp = record.GetTime().Value.ToUnixTimeTicks(),
                            Field = record.GetField().ToString(),
                            Value = record.GetValue().ToString()
                        }));
            });
            return results;
        }

        [HttpPost]
        public ActionResult<MonitoringData> Create(MonitoringDataRequest request)
        {
            foreach (MonitoringData data in request.MonitoringDataList)
            {
                _influxService.Write(write =>
                {
                    var point = PointData.Measurement("monitoringData")
                        .Tag("monitoring", "test-monitoring")
                        .Field("pressure", data.Pressure)
                        .Field("temperature", data.Temperature)
                        .Timestamp(data.Timestamp, WritePrecision.Ms);

                    write.WritePoint(point, "test-bucket", "organization");
                });
            }

            return Ok();
        }
    }
}
