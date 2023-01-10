using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Text.Json.Serialization;

namespace Streampipes_API.Models
{
    public class Warning
    {
        [JsonPropertyName("warning")]
        public int Value { get; set; }

        [JsonPropertyName("timestamp")]
        public long Timestamp { get; set; }
        [JsonPropertyName("value")]
        public double Outlier { get; set; }
    }
}
