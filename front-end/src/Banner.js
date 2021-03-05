import React from 'react';
import './Banner.css';
import { pages } from './App.js';
import { postLogout } from './api.js';

// A banner component
class Banner extends React.Component{
  constructor(props){
    super(props);
    this.handleHome = this.handleHome.bind(this);
    this.handleLogin = this.handleLogin.bind(this);
    this.handleLogout = this.handleLogout.bind(this);
  }

  // handle click "go to login page" button
  handleLogin(e){
    this.props.callback({
      page: pages.login,
      user: null
    });
  }

  // handle logout
  // If everything OK then change the page of the App and remove user
  // else alert user
  handleLogout(e){
    if(this.props.user !== null){
      let reqObj = this.props.user.token;
      postLogout(reqObj)
        .then(
          json => {
            setTimeout(() => {
              this.props.callback({
                page: pages.main,
                user: null
              })
            }, 0)
          })
          .catch(err => {
            alert("Got a problem while trying to logout user " + this.props.user.username + ":\n" + err.message);
          });
    }
  }

  handleHome(e){
    this.props.callback({
      page: pages.main
    });
  }

  render(){
    let userName = this.props.user ? this.props.user.username : "<anonymous>";
    return(
      <div className="banner">
        <h4>EvCharge</h4>
        {this.props.user !== null &&(
          <div className="dropdown">
            <button
              type="button"
              className="user_btn"
              name="user_btn"
            >
              {userName}
            </button>
            <p onClick={this.handleLogout}>
              Logout
            </p>
          </div>
        )}
        {this.props.user === null &&(
          <button
            type="button"
            className="login_btn"
            name="login"
            onClick={this.handleLogin}
          >
            Login
          </button>
        )}
        <button
          type="button"
          name="home"
          onClick={this.handleHome}
          > Home
        </button>
      </div>
    );
  }
}

export default Banner;
