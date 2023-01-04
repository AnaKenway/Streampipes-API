using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace Streampipes_API.Models
{
    public class MonitoringDataRequest
    {
        [JsonPropertyName("data_array")]
        public List<MonitoringData> MonitoringDataList { get; set; }
        [JsonPropertyName("timestamp")]
        public long Timestamp { get; set; }
    }
}
