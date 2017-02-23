import Vue from 'vue';

export const Todo = Vue.component('todo', {
  props: ['todo', 'index'],
  data: () => ({}),
  template: `\
      <li \
        v-on:click="strikeItem(index)" \
        v-bind:class="{ striked: todo.strike }"\
        >{{ todo.text }}</li> \
  `,
  methods: {
    strikeItem: function(i) {
      this.todo.strike = !this.todo.strike;
    },
  }
});
