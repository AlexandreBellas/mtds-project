// Libraries
const express = require('express')
const cors = require('cors')
const bodyParser = require('body-parser')
const kafka = require('../kafka')
require('dotenv').config()

console.log('[MAIN-SERVER] Initializing producer...')

// App configuration
const app = express()
const port = process.env.SERVER_PORT
const topicName = process.env.TOPIC
const numPartitions = process.env.NUM_PROCESSES

app.use(cors())
app.use(bodyParser.json({ type: 'application/json' }))

const taskMap = [
  'image-compression',
  'text-formatting',
  'conversion-of-file',
  'voice-registration'
]

// Kafka configuration
let producer = null

const kafkaConfig = async () => {
  // Admin configuration
  const admin = kafka.admin()
  await admin.connect()

  const topics = await admin.listTopics()

  if (!topics.includes(topicName)) {
    console.log(`[MAIN-SERVER] Created topic '${topicName}'`)
    await admin.createTopics({
      topics: [{ topic: topicName, numPartitions }]
    })
  }

  // Producer configuration
  const partitioner = () => {
    return () => Math.floor(Math.random() * taskMap.length)
  }

  producer = kafka.producer({
    createPartitioner: partitioner
  })

  await producer.connect()
}

kafkaConfig()

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

  let resp = []

  while (Array.isArray(resp) && resp.length === 0) {
    resp = await producer.send({
      topic: topicName,
      messages: [
        {
          value: JSON.stringify({
            task,
            params,
            directory
          }),
          partition: taskMap.indexOf(task)
        }
      ]
    })
  }

  console.log('[MAIN-SERVER] Successfully created messages: ', resp)

  return res.json({ message: 'success' })
})

app.listen(port, () => {
  console.log(`[MAIN-SERVER] Service available at http://localhost:${port}`)
})
