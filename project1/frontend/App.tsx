import React from 'react'
import {NavbarComponent} from './src/Navbar'
import {Box, Container, CssBaseline} from '@mui/material'
import {makeStyles} from '@mui/styles'
const useStyles = makeStyles({
     root: {
          border: 0,
          borderRadius: 3
     }
})

const App = () => {
     const classes = useStyles()

     // var map = L.map('map').setView([51.505, -0.09], 13)

     return (
          <div className="">
               <CssBaseline>
                    <NavbarComponent />
                    <Container>
                         <Box>sds</Box>
                         <div></div>
                    </Container>
               </CssBaseline>
          </div>
     )
}

export default App
