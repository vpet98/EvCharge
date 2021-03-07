import React from 'react';
import {user_roles} from '../app_essentials/App.js';
import {creditCardPayment} from '../api_comm/api.js';
import './Payment.css';
import {loadStripe} from '@stripe/stripe-js'
import {
  BrowserRouter as Router,
  Switch,
  Route,
  Link,
  useRouteMatch,
  useParams
} from "react-router-dom";

const stripePromise = loadStripe('pk_test_51IRF2wCiDDET7BaUBVnorIxVLt8UCxx7ypI7c3RIW70GKYHeo9qFX2dL18L4UDWKwDtdFHdVrR8bOz5aOcXqMMvL00sAQS7Ui0');

class Payment extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      cash: true
    };
    this.handlePaymentMethod = this.handlePaymentMethod.bind(this);
  };

  handlePaymentMethod(e) {
    const new_state = e.target.name === "cash" ? true : false;
    this.setState({ cash: new_state});
  };

  render(){
    return (
      <div>
      {this.props.state.cost !== '0' &&(
        <div className="Payment">
        <button
          type="button"
          name="cash"
          value={this.state.cash}
          onClick={this.handlePaymentMethod}
          > Cash
        </button>
        <button
          type="button"
          name="credit_card"
          value={!this.state.cash}
          onClick={this.handlePaymentMethod}
        > Credit Card
        </button>
        {this.state.cash &&(
          <Cash
            user={this.props.user}
            handleCheckout={this.props.handleCheckout}/>
        )}
        {!this.state.cash &&(
          <CreditCard
            user={this.props.user}
            state={this.props.state}
            handleCheckout={this.props.handleCheckout}/>
        )}
      </div>
    )}
    {this.props.state.cost === '0' &&(
      <button
        type="button"
        name="Checkout"
        onClick={this.props.handleCheckout}
        > Finish Charging
      </button>
    )}
    </div>
    )}
}

class Cash extends React.Component{
  render(){
    let noCash = this.props.user !== null && this.props.user.hasOwnProperty('role')
                            && this.props.user.role !== user_roles.operator;
    return(
      <div className="Cash">
      {!noCash &&(
        <div>
          <p>Press "Finish Charging" button to terminate charging session.</p>
          <button
            type="button"
            name="Checkout"
            onClick={this.props.handleCheckout}
            > Finish Charging
          </button>
        </div>
      )}
      {noCash &&(
        <h5>You must be an operator to accept payment in cash.</h5>
      )}
      </div>
    );
  }
}

class CreditCard extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      cash: true,
      error: null
    };
    this.handlePayment = this.handlePayment.bind(this);
  };

  handlePayment(e){
    const stripe = window.Stripe('pk_test_51IRF2wCiDDET7BaUBVnorIxVLt8UCxx7ypI7c3RIW70GKYHeo9qFX2dL18L4UDWKwDtdFHdVrR8bOz5aOcXqMMvL00sAQS7Ui0');
    let info = {
      sessionId: this.props.state.sessionId,
      token: this.props.user.token
    }
    creditCardPayment(info)
      .then(json => {
        setTimeout(() => {
          console.log(json);
          if (!json.data.Response){
            const res = stripe.redirectToCheckout({
              sessionId : json.data.session
            });
            if(res.error){
              this.setState({error: res.error.message});
            }
            else{
              this.props.handleCheckout();
            }
          }
          else
            this.setState({ error: json.data.Response });
        }, 0)
      })
      .catch(err =>{
        this.setState({ error: err.response.data.message });
      });

}

  render(){
    return(
      <div className="CreditCard">
        <p>Credit Card Info</p>
        <button
          type="button"
          name="pay"
          role="link"
          onClick={this.handlePayment}
        > Pay
        </button>
        {this.state.error &&(
          <p>{this.state.error}</p>
        )}
      </div>
    );
  }
}

export default Payment;
