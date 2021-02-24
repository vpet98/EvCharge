import React from 'react';
import './App.css';
import Banner from './Banner.js';
import ServicesDiv from './ServicesDiv.js';
import Footer from './Footer.js';
import Login from './Login.js';

// an enumeration of all possible pages
export const pages = {
  main: "main",
  login: "login"
};

// the most general Component that manages the different pages
class App extends React.Component {
  constructor(props){
    super(props);
    this.state = {
      user: props.user,
      page: pages.main
    };
    this.changePage = this.changePage.bind(this);
  }

  // a function to change the state (page, user)
  changePage(props){
    if(props.user !== null)
      this.setState({ user: props.user });
    if(props.page !== null)
      this.setState({ page: props.page });
  }

  render(){
    return (
      <div className="container">
      {this.state.page === pages.main && (
        <div className="mainPage">
          <Banner user={this.state.user} callback={this.changePage}/>
          <h1>Ev Charge</h1>
          <ServicesDiv user={this.state.user}/>
          <Footer/>
        </div>
      )}
      {this.state.page === pages.login && (
        <Login callback={this.changePage}/>
      )}
      </div>
    );
  }
}

export default App;
