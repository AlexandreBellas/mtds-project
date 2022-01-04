// Libraries
const express = require('express')
const cors = require('cors')
const bodyParser = require('body-parser')
const kafka = require('../kafka')
require('dotenv').config()

// App configuration
const app = express()
const port = process.env.SERVER_PORT
const producer = kafka.producer()
producer.connect()

app.use(cors())
app.use(bodyParser.json({ type: 'application/json' }))

// Middleware
app.use((req, res, next) => {
  console.log(
    `[MAIN-SERVER] Request on '${req.path}' with task '${
      req.query.task
    }' at ${new Date().toISOString()}`
  )
  next()
})

// Routes
app.post('/compute', async (req, res) => {
  const { task, params, directory } = req.body

  const resp = await producer.send({
    topic: 'task',
    messages: [
      {
        value: JSON.stringify({
          task,
          params,
          directory
        })
      }
    ]
  })

  console.log('Created messages: ', resp)

  return res.send()
})

app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`)
})
