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
    public class WeatherForecastController : ControllerBase
    {
        private static readonly string[] Summaries = new[]
        {
            "Freezing", "Bracing", "Chilly", "Cool", "Mild", "Warm", "Balmy", "Hot", "Sweltering", "Scorching"
        };

        private readonly ILogger<WeatherForecastController> _logger;
        private readonly WeatherService _weatherService;

        public WeatherForecastController(ILogger<WeatherForecastController> logger, WeatherService weatherService)
        {
            _logger = logger;
            _weatherService = weatherService;
        }

        [HttpGet]
        public ActionResult<List<WeatherForecast>> Get() =>
            _weatherService.Get();

        [HttpGet("{id:length(24)}", Name = "GetWeather")]
        public ActionResult<WeatherForecast> Get(string id)
        {
            var weather = _weatherService.Get(id);

            if (weather == null)
            {
                return NotFound();
            }

            return weather;
        }

        [HttpPost]
        public ActionResult<WeatherForecast> Create(WeatherForecast weather)
        {
            _weatherService.Create(weather);

            return CreatedAtRoute("GetWeather", new { id = weather.Id.ToString() }, weather);
        }

        [HttpPut("{id:length(24)}")]
        public IActionResult Update(string id, WeatherForecast weatherIn)
        {
            var weather = _weatherService.Get(id);

            if (weather == null)
            {
                return NotFound();
            }

            _weatherService.Update(id, weatherIn);

            return NoContent();
        }

        [HttpDelete("{id:length(24)}")]
        public IActionResult Delete(string id)
        {
            var weather = _weatherService.Get(id);

            if (weather == null)
            {
                return NotFound();
            }

            _weatherService.Remove(id);

            return NoContent();
        }
    }
}
