import React from 'react';
import './Login.css';
import {pages} from '../app_essentials/App.js';
import {postLoginUser} from '../api_comm/api.js';
import AppiErrorHandler from '../api_comm/error_handling.js';

// the login page component
class Login extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      username: "",
      password: "",
      message: null,
      error: null
    };
    this.handleInput = this.handleInput.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleReturn = this.handleReturn.bind(this);
    this.handleKeyDown = this.handleKeyDown.bind(this);
  }

  // handle any change in input areas
  handleInput(e){
    const name = e.target.name;
    const value = e.target.value;
    this.setState({ [name]: value });
  }

  // submit form on enter pressed
  handleKeyDown(e){
    if (e.key === 'Enter') {
      this.handleSubmit(e);
    }
  }

  // handle the submit button of the form
  // If everything OK then change the page of the App and return user
  // else return error
  handleSubmit(e){
    this.setState({ message: "Logging in...", error: null });
    if(this.state.username === ""){
      this.setState({ message: null, error: "You have to fill in your username" });
    }else if(this.state.password === ""){
      this.setState({ message: null, error: "You have to fill in your password" });
    }else{
      let reqObj = {
        username: this.state.username,
        password: this.state.password
      };
      postLoginUser(reqObj)
        .then(json => {
          setTimeout(() => {
            let finalUser = {
              username: this.state.username,
              password: this.state.password, // maybe this is not a good IDEA
              token: json.data.token,
              roles: json.data.roles
            };
            localStorage.setItem('user', JSON.stringify(finalUser));
            this.props.callback({
              page: pages.main,
              user: finalUser
            });
          }, 0)
        })
        .catch(error => {
          let handler = new AppiErrorHandler(error);
          let txt = handler.getMessage();
          if(txt !== null){
            if(handler.getError() !== null) txt = txt + '\n' + handler.getError();
          }else txt = handler.getError();
          this.setState({ message: null, error: txt })
        });
    }
  }

  // handle the return button of the form to return to the main page without loging in
  handleReturn(e){
    this.setState({ error: null });
    this.props.callback({
      page: pages.main,
      user: null
    });
  }

  render(){
    return(
      <div className="login_page">
        <h4>EvCharge</h4>
        <h4>Login and start Charging</h4>
          <div className="login_div">
            <p>Username</p>
            <input
              type="text"
              name="username"
              field="username"
              placeholder="username"
              value={this.state.username}
              onChange={this.handleInput}
              onKeyDown={this.handleKeyDown}
            />
            <p>Password</p>
            <input
              type="password"
              name="password"
              field="password"
              placeholder="password"
              value={this.state.password}
              onChange={this.handleInput}
              onKeyDown={this.handleKeyDown}
            />
            <button
              type="button"
              name="login"
              className="btn waves-effect waves-light"
              onClick={this.handleSubmit}
            >
              Login
            </button>
            <button
              type="button"
              name="return"
              className="btn waves-effect waves-light"
              onClick={this.handleReturn}
            >
              Continue as guest
            </button>
            {this.state.message !== null && (
              <p>{this.state.message}</p>
            )}
            {this.state.error !== null && (
              <div className="error"><p>{this.state.error}</p></div>
            )}
          </div>
      </div>
    );
  }
}

export default Login;
