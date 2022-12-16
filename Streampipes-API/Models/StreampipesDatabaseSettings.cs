using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Streampipes_API.Interfaces;

namespace Streampipes_API.Models
{
    public class StreampipesDatabaseSettings : IStreampipesDatabaseSettings
    {
        public string ConnectionString { get; set; }

        public string DatabaseName { get; set; } 

        public string CollectionName { get; set; } 
    }
}
