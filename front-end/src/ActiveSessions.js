import React from 'react';
import './ActiveSessions.css';
import {getSessions} from './api.js';

class ActiveSessions extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      active_sessions : []
    };
    this.getsessions = this.getsessions.bind(this);
  }

  componentDidMount(){
    this.getsessions();
  }

  getsessions(){
    getSessions(this.props.user.token)
      .then(json => {
        setTimeout(() => {
          console.log(json);
          if (!json.data.Response){
            this.setState({ active_sessions: json.data.ActiveSessionsList });
          }
          else
            this.props.handleInput({target: {name: "error", value: json.data.Response }});
        }, 0)
      })
      .catch(err =>{
        this.props.handleInput({target: {name: "error", value: err.response.data.message }});
      });
  }

  render(){
    return(
      <div className="Sessions">
        {this.state.active_sessions.map((item,i) =>
          <button
            key={i}
            type="button"
            name="sessionId"
            value={item.SessionID}
            onClick={this.props.handleInput}
          > SessionID: {item.SessionID}, Cost: {item.CurrentCost}
          </button>
        )}
      </div>
    );
  }
}

export default ActiveSessions;
