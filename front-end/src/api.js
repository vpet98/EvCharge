import axios from 'axios';
import config from './config'
import qs from 'qs';

axios.defaults.baseURL = config.apiUrl;
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';


export const postLoginToken = obj => {
  const url = '/login';
  const data = qs.stringify(obj);
  return axios.post(url, data);
}
