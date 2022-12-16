using System;
using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace Streampipes_API.Models
{
    public class WeatherForecast
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }
        [JsonPropertyName("Date")]
        public DateTime Date { get; set; }

        [JsonPropertyName("TemperatureC")]
        public int TemperatureC { get; set; }

        [JsonPropertyName("Summary")]
        public string Summary { get; set; }
    }
}
