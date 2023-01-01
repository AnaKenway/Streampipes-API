using System;
using System.Threading.Tasks;
using InfluxDB.Client;
using Microsoft.Extensions.Configuration;

namespace Streampipes_API.Services
{
    public class InfluxDBService
    {
        private readonly string _token;

        public InfluxDBService(IConfiguration configuration)
        {
            _token = configuration.GetValue<string>("InfluxDB:Token");
        }

        public void Write(Action<WriteApi> action)
        {
            //using var client = InfluxDBClientFactory.Create("http://localhost:8086", _token);
            using var client = InfluxDBClientFactory.Create("http://host.docker.internal:8086", _token);
            using var write = client.GetWriteApi();
            action(write);
        }

        public async Task<T> QueryAsync<T>(Func<QueryApi, Task<T>> action)
        {
            //using var client = InfluxDBClientFactory.Create("http://localhost:8086", _token);
            using var client = InfluxDBClientFactory.Create("http://host.docker.internal:8086", _token);
            var query = client.GetQueryApi();
            return await action(query);
        }
    }
}
//Timestamp = long.Parse(record.GetTime().Value.ToString()),
//                            InfluxId = record.Values["influx-id"].ToString(),
//                            Temperature = double.Parse(record.Values["temperature"].ToString()),
//                            Pressure = double.Parse(record.Values["pressure"].ToString())
//write.WriteRecord($"monitoringData,monitoring=test-monitoring influx-id={data.InfluxId},temperature={data.Temperature},pressure={data.Pressure} {data.Timestamp}", WritePrecision.Ns, "test-bucket", "organization");