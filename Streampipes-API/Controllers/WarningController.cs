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
    public class WarningController : ControllerBase
    {
        private readonly InfluxDBService _influxService;
        public WarningController(InfluxDBService influx)
        {
            _influxService = influx;
        }

        [HttpPost]
        public ActionResult<MonitoringData> Create(Warning data)
        {
            _influxService.Write(write =>
            {
                var point = PointData.Measurement("monitoringData")
                    .Tag("security", "warning")
                    .Field("value", data.Value)
                    .Timestamp(data.Timestamp, WritePrecision.Ms);

                write.WritePoint(point, "test-bucket", "organization");
            });

            return Ok();
        }
    }
}
