using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Streampipes_API.Models;
using Streampipes_API.Interfaces;
using Streampipes_API.Services;

namespace Streampipes_API.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class MonitoringDataController : ControllerBase
    {
        private readonly ILogger<MonitoringDataController> _logger;
        private readonly MonitoringDataService _monitoringService;

        public MonitoringDataController(ILogger<MonitoringDataController> logger, MonitoringDataService monitoringService)
        {
            _logger = logger;
            _monitoringService = monitoringService;
        }

        [HttpGet]
        public ActionResult<List<MonitoringData>> Get() =>
            _monitoringService.Get();

        [HttpGet("{id:length(24)}", Name = "GetData")]
        public ActionResult<MonitoringData> Get(string id)
        {
            var data = _monitoringService.Get(id);

            if (data == null)
            {
                return NotFound();
            }

            return data;
        }

        [HttpPost]
        public ActionResult<MonitoringData> Create(MonitoringData data)
        {
            _monitoringService.Create(data);

            return CreatedAtRoute("GetData", new { id = data.Id.ToString() }, data);
        }

        [HttpPut("{id:length(24)}")]
        public IActionResult Update(string id, MonitoringData dataIn)
        {
            var data = _monitoringService.Get(id);

            if (data == null)
            {
                return NotFound();
            }

            _monitoringService.Update(id, dataIn);

            return NoContent();
        }

        [HttpDelete("{id:length(24)}")]
        public IActionResult Delete(string id)
        {
            var data = _monitoringService.Get(id);

            if (data == null)
            {
                return NotFound();
            }

            _monitoringService.Remove(id);

            return NoContent();
        }
    }
}
