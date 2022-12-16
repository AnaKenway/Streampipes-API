using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Options;
using MongoDB.Driver;
using Streampipes_API.Models;
using Streampipes_API.Interfaces;

namespace Streampipes_API.Services
{
    public class WeatherService
    {
        private readonly IMongoCollection<WeatherForecast> _weatherReports;

        public WeatherService(IStreampipesDatabaseSettings settings)
        {
            var client = new MongoClient(settings.ConnectionString);
            var database = client.GetDatabase(settings.DatabaseName);

            _weatherReports = database.GetCollection<WeatherForecast>(settings.CollectionName);
        }
        public List<WeatherForecast> Get() =>
            _weatherReports.Find(report => true).ToList();

        public WeatherForecast Get(string id) =>
            _weatherReports.Find<WeatherForecast>(report => report.Id == id).FirstOrDefault();

        public WeatherForecast Create(WeatherForecast report)
        {
            _weatherReports.InsertOne(report);
            return report;
        }

        public void Update(string id, WeatherForecast reportIn) =>
            _weatherReports.ReplaceOne(report => report.Id == id, reportIn);

        public void Remove(WeatherForecast reportIn) =>
            _weatherReports.DeleteOne(report => report.Id == reportIn.Id);

        public void Remove(string id) =>
            _weatherReports.DeleteOne(report => report.Id == id);
    }
}
