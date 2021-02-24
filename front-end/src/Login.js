import React from 'react';
import './Login.css';
import {postLoginToken} from './api.js';

class Login extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      username: "",
      password: "",
      error: null,
      token: null
    };
    this.handleInput = this.handleInput.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleInput(e){
    const name = e.target.name;
    const value = e.target.value;
    this.setState({ [name]: value });
  }

  handleSubmit(e){
    this.setState({ error: null });
    if(this.state.username === ""){
      this.setState({ error: "You have to fill in your username" });
    }else if(this.state.password === ""){
      this.setState({ error: "You have to fill in your password" });
    }else{
      let reqObj = {
        username: this.state.username,
        password: this.state.password
      };
      postLoginToken(reqObj)
        .then(json => {
          setTimeout(() => {
            this.setState({ token: json.data.token });
            window.alert("Got token " + this.state.token);
          }, 0)
        })
        .catch(err => {
          this.setState({ error: err.error });
        });
    }
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
            />
            <p>Password</p>
            <input
              type="text"
              name="password"
              field="password"
              placeholder="password"
              value={this.state.password}
              onChange={this.handleInput}
            />
            <button
              type="button"
              name="login"
              onClick={this.handleSubmit}
            >
              Login
            </button>
            <p>{ this.state.error }</p>
          </div>
      </div>
    );
  }
}

export default Login;
