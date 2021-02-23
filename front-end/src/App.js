import React from 'react';
import './App.css';
import Banner from './Banner.js';
import ServicesDiv from './ServicesDiv.js';
import Footer from './Footer.js';


class App extends React.Component {
  constructor(props){
    super(props);
    this.state = { user: props.user };
  }

  render(){
    // let userName = this.state.user === null ? "<anonymous>" : this.state.user;
    return (
      <div className="container">
        <Banner user={this.state.user}/>
        <h1>Ev Charge</h1>
        <ServicesDiv user={this.state.user}/>
        <Footer/>
      </div>
    );
  }
}

export default App;
