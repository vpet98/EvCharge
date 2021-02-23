import React from 'react';
import './Banner.css';
import Login from './Login.js';
import ReactDOM from 'react-dom';


class Banner extends React.Component{
  constructor(props){
    super(props);
    this.state = { user : props.user };
    this.handleLogin = this.handleLogin.bind(this);
  }

  handleLogin(e){
    ReactDOM.render(
      <React.StrictMode>
        <Login/>
      </React.StrictMode>,
      document.getElementById('root')
    );

  }

  render(){
    let userName = this.state.user === null ? "<anonymous>" : this.state.user;
    return(
      <div className="banner">
        <h4>EvCharge</h4>
        {this.state.user !== null &&(
          <p>User: {userName}</p>
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
