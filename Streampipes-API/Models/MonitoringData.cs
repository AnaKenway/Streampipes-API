using System;
using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace Streampipes_API.Models
{
    public class MonitoringData
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }

        [JsonPropertyName("timestamp")]
        //in milliseconds, unix epoch time
        public long Timestamp { get; set; }

        [JsonPropertyName("temperature")]
        public double? Temperature { get; set; }

        [JsonPropertyName("pressure")]
        public double? Pressure { get; set; }
    }
}
