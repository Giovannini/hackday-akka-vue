import Vue from 'vue';
import { Todo } from './Todo';

export const TodoList = Vue.component('todo-list', {
  data: () => ({
    todoList: [
      { text: 'Make a Todolist', strike: false },
      { text: 'Eat another crepe', strike: false },
      { text: 'Phone my mommy', strike: false },
    ],
    newTodo: '',
  }),
  template: `\
    <div>\
      <todo \
        v-for="(item, index) in this.todoList" \
        v-bind:todo="item"\
        v-bind:index="index"\
      ></todo> \
      <input @keyup.enter="addNewTodo" v-model="newTodo"> \
    </div>\
  `,
  methods: {
    addNewTodo: function() {
      this.todoList = [...this.todoList, { text: this.newTodo }];
      this.newTodo = '';
    }
  }
});
