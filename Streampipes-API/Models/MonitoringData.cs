using System;
using System.Text.Json.Serialization;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using InfluxDB.Client.Core;


namespace Streampipes_API.Models
{
    public class MonitoringData
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }

        [JsonPropertyName("timestamp")]
        [Column(IsTimestamp = true)]

        public long Timestamp { get; set; }

        [JsonPropertyName("temperature")]
        [Column("temperature")]
        public double? Temperature { get; set; }

        [JsonPropertyName("pressure")]
        [Column("pressure")]
        public double? Pressure { get; set; }
    }
}
