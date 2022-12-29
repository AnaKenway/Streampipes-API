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
        public  Task<IEnumerable<MonitoringData>> Get()
        {
            //_mongoDBService.Get();
            return  _influxService.QueryAsync(async query =>
            {
                var flux = "from(bucket:\"test-bucket\") |> range(start: 0)";
                var tables = await query.QueryAsync(flux, "organization");
                return tables.SelectMany(table =>
                    table.Records.Select(record =>
                        new MonitoringData
                        {
                            //Timestamp = record.GetTime().Value,
                            InfluxId = record.GetValue().ToString(),
                            Temperature = double.Parse(record.GetValue().ToString()),
                            Pressure = double.Parse(record.GetValue().ToString())
                        }));
            });
        }
            

        [HttpGet("{id:length(24)}", Name = "GetData")]
        public ActionResult<MonitoringData> Get(string id)
        {
            var data = _mongoDBService.Get(id);

            if (data == null)
            {
                return NotFound();
            }

            return data;
        }

        [HttpPost]
        public ActionResult<MonitoringData> Create(MonitoringData data)
        {
            //_mongoDBService.Create(data);
            _influxService.Write(write =>
            {
                var point = PointData.Measurement("monitoringData")
                    .Tag("monitoring", "test-monitoring")
                    .Field("influx-id",data.InfluxId)
                    .Field("pressure", data.Pressure)
                    .Field("temperature",data.Temperature)
                    .Timestamp(data.Timestamp, WritePrecision.Ns);

                write.WritePoint(point, "test-bucket", "organization");
            });


            return CreatedAtRoute("GetData", new { id = data.InfluxId.ToString() }, data);
        }

        [HttpPut("{id:length(24)}")]
        public IActionResult Update(string id, MonitoringData dataIn)
        {
            var data = _mongoDBService.Get(id);

            if (data == null)
            {
                return NotFound();
            }

            _mongoDBService.Update(id, dataIn);

            return NoContent();
        }

        [HttpDelete("{id:length(24)}")]
        public IActionResult Delete(string id)
        {
            var data = _mongoDBService.Get(id);

            if (data == null)
            {
                return NotFound();
            }

            _mongoDBService.Remove(id);

            return NoContent();
        }
    }
}
