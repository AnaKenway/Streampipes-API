using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Streampipes_API.Services;
using Streampipes_API.Models;
using InfluxDB.Client.Writes;
using InfluxDB.Client.Api.Domain;

namespace Streampipes_API.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class CriticalAlarmController : ControllerBase
    {
        private readonly InfluxDBService _influxService;
        public CriticalAlarmController(InfluxDBService influx)
        {
            _influxService = influx;
        }

        [HttpPost]
        public ActionResult<MonitoringData> Create(CriticalAlarm data)
        {
            _influxService.Write(write =>
            {
                var point = PointData.Measurement("monitoringData")
                    .Tag("security", "critical-alarm")
                    .Field("value", data.Value)
                    .Field("outlier", data.Outlier)
                    .Timestamp(data.Timestamp, WritePrecision.Ms);

                write.WritePoint(point, "test-bucket", "organization");
            });

            return Ok();
        }
    }
}
