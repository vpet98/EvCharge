import React from 'react';
import './Banner.css';
import { pages } from './App.js';

// A banner component
class Banner extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      user: props.user,
      appCallback: props.callback,
    };
    this.handleLogin = this.handleLogin.bind(this);
  }

  // handle click "go to login page" button
  handleLogin(e){
    this.state.appCallback({
      page: pages.login,
      user: null
    });
  }

  render(){
    let userName = this.state.user ? this.state.user.username : "<anonymous>";
    return(
      <div className="banner">
        <h4>EvCharge</h4>
        {this.state.user !== null &&(
          <p>{userName}</p>
        )}
        {this.state.user === null &&(
          <button
            type="button"
            className="login_btn"
            name="login"
            onClick={this.handleLogin}
          >
            Login
          </button>
        )}
      </div>
    );
  }
}

export default Banner;
