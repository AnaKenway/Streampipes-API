using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Streampipes_API.Models
{
    public class MonitoringDataResponse
    {
        public long Timestamp { get; set; }
        public string Field { get; set; }
        public string Value { get; set; }
    }
}
