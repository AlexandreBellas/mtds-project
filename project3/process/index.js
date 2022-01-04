// Libraries
const kafka = require('../kafka')
const fs = require('fs')
const path = require('path')
require('dotenv').config()

// App configuration
const consumer = kafka.consumer({
  groupId: process.env.GROUP_ID
})

function calculate(task, params) {
  const time = Math.ceil(Math.random() * process.env.MAX_TASK_TIME) + 1
  return new Promise((resolve) => setTimeout(resolve, time))
}

// Execution
const main = async () => {
  await consumer.connect()
  await consumer.subscribe({
    topic: 'task',
    fromBeginning: true
  })

  await consumer.run({
    eachMessage: ({ topic, partition, message }) => {
      const key = message.key ? message.key.toString() : null
      const value = message.value ? JSON.parse(message.value.toString()) : null

      if (value) {
        console.log('Received message:', {
          topic,
          partition,
          key,
          value
        })

        const { task, params, directory } = value

        const directoryPath = path.join(
          __dirname,
          '..',
          `resources/${directory}`
        )
        const filePath = `${directoryPath}/result.txt`
        if (!fs.existsSync(directoryPath)) {
          fs.mkdirSync(directoryPath, { recursive: true })
        }

        calculate(task, params).then(() => {
          try {
            fs.writeFileSync(
              filePath,
              `Task '${task}' computed with params '${params}'.`
            )

            console.log(`Result for task '${task}' stored in path ${filePath}.`)
          } catch (err) {
            const detail = `Failed to save results of task ${task}: ${err.message}`
            const error = new Error(detail)
            error.detail = detail
            error.code = 'ERR_SAVE_RESULT_FAILURE'

            throw err
          }
        })
      } else {
        console.log('Received a null message.')
      }
    }
  })
}

main().catch(async (error) => {
  console.error(error)

  try {
    await consumer.disconnect()
  } catch (err) {
    console.error('Failed to disconnect consumer: ', err)
  }

  process.exit(1)
})
