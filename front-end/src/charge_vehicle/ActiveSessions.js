import React from 'react';
import './ActiveSessions.css';
import {getSessions} from '../api_comm/api.js';

class ActiveSessions extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      active_sessions : []
    };
    this.getsessions = this.getsessions.bind(this);
    this.chooseSession = this.chooseSession.bind(this);
  }

  componentDidMount(){
    this.getsessions();
  }

  chooseSession(e){
    const session = {target: {name: "sessionId", value: e.target.name}};
    const cost = {target: {name: "cost", value: e.target.value}};
    this.props.handleInput(session);
    this.props.handleInput(cost);
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
            name={item.SessionID}
            value={item.CurrentCost}
            onClick={this.chooseSession}
          > SessionID: {item.SessionID}, Cost: {item.CurrentCost}
          </button>
        )}
        <button
          type="button"
          name="Refresh"
          onClick={this.getsessions}
        > Refresh
        </button>
      </div>
    );
  }
}

export default ActiveSessions;
