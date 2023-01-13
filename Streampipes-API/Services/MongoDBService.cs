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
    public class MongoDBService
    {
        private readonly IMongoCollection<MonitoringData> _monitoringData;

        public MongoDBService(IStreampipesDatabaseSettings settings)
        {
            var client = new MongoClient(settings.ConnectionString);
            var database = client.GetDatabase(settings.DatabaseName);

            _monitoringData = database.GetCollection<MonitoringData>(settings.DataCollectionName);
        }
        public List<MonitoringData> Get() =>
            _monitoringData.Find(data => true).ToList();

        public MonitoringData Get(string id) =>
            _monitoringData.Find<MonitoringData>(d => d.Id == id).FirstOrDefault();

        public MonitoringData Create(MonitoringData data)
        {
            _monitoringData.InsertOne(data);
            return data;
        }

        public void Update(string id, MonitoringData data) =>
            _monitoringData.ReplaceOne(data => data.Id == id, data);

        public void Remove(MonitoringData data) =>
            _monitoringData.DeleteOne(d => d.Id == data.Id);

        public void Remove(string id) =>
            _monitoringData.DeleteOne(d => d.Id == id);
    }
}
