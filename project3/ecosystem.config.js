require('dotenv').config()

module.exports = {
  apps: [
    {
      name: 'server',
      script: './server/index.js'
    },
    {
      name: 'process',
      script: './process/index.js',
      instances: process.env.NUM_PROCESSES
    }
  ]
}
